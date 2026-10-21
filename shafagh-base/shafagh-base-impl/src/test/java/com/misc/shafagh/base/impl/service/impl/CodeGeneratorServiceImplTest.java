package com.misc.shafagh.base.impl.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.base.impl.service.api.CodeNumberStore;
import com.misc.shafagh.base.api.CodeGenerationException;
import com.misc.shafagh.base.api.CodeGenerationException.Reason;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

/** Service transaction tests. Oracle-specific SQL is covered separately, not emulated by H2. */
class CodeGeneratorServiceImplTest {
    private JdbcTemplate jdbc;
    private TransactionTemplate registration;
    private CodeGeneratorServiceImpl service;

    @BeforeEach
    void setup() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE branch_counter (scope_code VARCHAR(11) PRIMARY KEY, next_number BIGINT NOT NULL)");
        jdbc.execute("CREATE TABLE account_counter (scope_code VARCHAR(11) PRIMARY KEY, next_number BIGINT NOT NULL)");
        jdbc.execute("CREATE SEQUENCE customer_sequence START WITH 1 MAXVALUE 99999999 NO CYCLE");
        JdbcTransactionManager manager = new JdbcTransactionManager(dataSource);
        registration = new TransactionTemplate(manager);
        service = new CodeGeneratorServiceImpl(new H2Store(jdbc), manager, 10);
    }

    @Test
    void firstNumbersAreOneAndScopesAreIndependent() {
        registration.executeWithoutResult(s -> {
            service.initializeBranchCounter("562210");
            service.initializeBranchCounter("562211");
            service.initializeAccountCounter("56221000001");
            service.initializeAccountCounter("56221100001");
        });
        assertEquals("56221000001", registration.execute(s -> service.generateBranchCode("562210")));
        assertEquals("56221000002", registration.execute(s -> service.generateBranchCode("562210")));
        assertEquals("56221100001", registration.execute(s -> service.generateBranchCode("562211")));
        String first = service.generateAccountNumber("56221000001");
        String other = service.generateAccountNumber("56221100001");
        assertEquals("56221000001000001", first.substring(0, 17));
        assertEquals("56221100001000001", other.substring(0, 17));
        assertValidLuhn(first);
        assertValidLuhn(other);
        assertEquals("56221000000001", service.generateCustomerCode("562210"));
        assertEquals("56221100000002", service.generateCustomerCode("562211"));
    }

    @Test
    void lastNumberIsIssuedOnceAndSentinelRemainsExhausted() {
        initialize();
        jdbc.update("UPDATE branch_counter SET next_number = 99999");
        jdbc.update("UPDATE account_counter SET next_number = 999999");
        assertEquals("56221099999", registration.execute(s -> service.generateBranchCode("562210")));
        String lastAccount = service.generateAccountNumber("56221000001");
        assertEquals("999999", lastAccount.substring(11, 17));
        assertValidLuhn(lastAccount);
        assertEquals(100000L, value("branch_counter"));
        assertEquals(1000000L, value("account_counter"));
        assertReason(Reason.CAPACITY_EXHAUSTED,
                () -> registration.execute(s -> service.generateBranchCode("562210")));
        assertReason(Reason.CAPACITY_EXHAUSTED, () -> service.generateAccountNumber("56221000001"));
        assertEquals(1000000L, value("account_counter"));
    }

    @Test
    void accountAllocationSurvivesOuterRegistrationRollback() {
        initialize();
        String reserved = registration.execute(status -> {
            // Acquire an actual outer JDBC connection before REQUIRES_NEW.
            jdbc.queryForObject("SELECT COUNT(*) FROM account_counter", Long.class);
            String number = service.generateAccountNumber("56221000001");
            status.setRollbackOnly();
            return number;
        });
        String next = service.generateAccountNumber("56221000001");
        assertEquals("000001", reserved.substring(11, 17));
        assertEquals("000002", next.substring(11, 17));
        assertNotEquals(reserved, next);
    }

    @Test
    void branchAllocationAndProvisioningRollBackWithRegistration() {
        initialize();
        registration.executeWithoutResult(status -> {
            assertEquals("56221000001", service.generateBranchCode("562210"));
            service.initializeAccountCounter("56221000002");
            status.setRollbackOnly();
        });
        assertEquals("56221000001", registration.execute(s -> service.generateBranchCode("562210")));
        assertReason(Reason.COUNTER_NOT_INITIALIZED, () -> service.generateAccountNumber("56221000002"));
        registration.executeWithoutResult(status -> {
            service.initializeBranchCounter("562211");
            status.setRollbackOnly();
        });
        assertReason(Reason.COUNTER_NOT_INITIALIZED,
                () -> registration.execute(s -> service.generateBranchCode("562211")));
    }

    @Test
    void repeatedProvisioningDoesNotResetAllocatedNumbers() {
        initialize();
        service.generateAccountNumber("56221000001");
        assertReason(Reason.COUNTER_ALREADY_INITIALIZED,
                () -> registration.executeWithoutResult(s -> service.initializeAccountCounter("56221000001")));
        assertEquals("000002", service.generateAccountNumber("56221000001").substring(11, 17));
    }

    @Test
    void missingOrReadOnlyRegistrationTransactionIsRejected() {
        assertThrows(IllegalTransactionStateException.class, () -> service.initializeBranchCounter("562210"));
        assertThrows(IllegalTransactionStateException.class, () -> service.initializeAccountCounter("56221000001"));
        assertThrows(IllegalTransactionStateException.class, () -> service.generateBranchCode("562210"));
        registration.setReadOnly(true);
        assertThrows(IllegalTransactionStateException.class,
                () -> registration.executeWithoutResult(s -> service.initializeBranchCounter("562210")));
        assertEquals(0L, jdbc.queryForObject("SELECT COUNT(*) FROM branch_counter", Long.class));
    }

    @Test
    void malformedInputsDoNotConsumeNumbers() {
        for (String value : new String[] {null, "12345", "1234567", "۱۲۳۴۵۶", "12345 ", "12345a"}) {
            assertThrows(IllegalArgumentException.class, () -> service.generateCustomerCode(value));
            assertThrows(IllegalArgumentException.class, () -> service.initializeBranchCounter(value));
        }
        for (String value : new String[] {null, "5622100000", "562210000001", "۵۶۲۲۱۰۰۰۰۰۱", "5622100000a"}) {
            assertThrows(IllegalArgumentException.class, () -> service.generateAccountNumber(value));
        }
        assertEquals("00000100000001", service.generateCustomerCode("000001"));
        assertReason(Reason.COUNTER_NOT_INITIALIZED, () -> service.generateAccountNumber("56221000001"));
    }

    @Test
    void concurrentAllocationsFromIndependentConnectionsRemainUnique() throws Exception {
        initialize();
        int threads = 8;
        int perThread = 20;
        var executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            var futures = new ArrayList<java.util.concurrent.Future<List<String>>>();
            for (int t = 0; t < threads; t++) {
                futures.add(executor.submit(() -> {
                    assertTrue(start.await(10, TimeUnit.SECONDS));
                    List<String> results = new ArrayList<>();
                    for (int i = 0; i < perThread; i++) results.add(service.generateAccountNumber("56221000001"));
                    return results;
                }));
            }
            start.countDown();
            var all = new HashSet<String>();
            for (var future : futures) {
                for (String number : future.get(30, TimeUnit.SECONDS)) {
                    assertTrue(all.add(number), "Duplicate account number");
                    assertValidLuhn(number);
                }
            }
            assertEquals(threads * perThread, all.size());
            assertEquals(threads * perThread + 1L, value("account_counter"));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private void initialize() {
        registration.executeWithoutResult(s -> {
            service.initializeBranchCounter("562210");
            service.initializeAccountCounter("56221000001");
        });
    }

    private long value(String table) {
        return jdbc.queryForObject("SELECT next_number FROM " + table, Long.class);
    }

    private static void assertReason(Reason expected, org.junit.jupiter.api.function.Executable action) {
        assertEquals(expected, assertThrows(CodeGenerationException.class, action).getReason());
    }

    private static void assertValidLuhn(String number) {
        assertTrue(number.matches("[0-9]{18}"));
        int[] weights = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};
        int total = 0;
        for (int i = 0; i < number.length(); i++) {
            int digit = number.charAt(i) - '0';
            total += i % 2 == 0 ? weights[digit] : digit;
        }
        assertEquals(0, total % 10, "Invalid Luhn checksum");
    }

    /** Test adapter exercises service transactions, while production uses Oracle RETURNING. */
    private static class H2Store implements CodeNumberStore {
        private final JdbcTemplate jdbc;
        H2Store(JdbcTemplate jdbc) { this.jdbc = jdbc; }
        @Override public long nextCustomerNumber() {
            return jdbc.queryForObject("SELECT NEXT VALUE FOR customer_sequence", Long.class);
        }
        @Override public long allocateBranchNumber(String code) {
            return allocate("branch_counter", code, MAX_BRANCH_NUMBER);
        }
        @Override public long allocateAccountNumber(String code) {
            return allocate("account_counter", code, MAX_ACCOUNT_NUMBER);
        }
        @Override public void initializeBranchCounter(String code) { initialize("branch_counter", code); }
        @Override public void initializeAccountCounter(String code) { initialize("account_counter", code); }
        private void initialize(String table, String code) {
            try {
                jdbc.update("INSERT INTO " + table + " VALUES (?, 1)", code);
            } catch (DuplicateKeyException e) {
                throw new CodeGenerationException(Reason.COUNTER_ALREADY_INITIALIZED, "Already initialized", e);
            }
        }
        private long allocate(String table, String code, long max) {
            var values = jdbc.query("SELECT next_number FROM " + table + " WHERE scope_code = ? FOR UPDATE",
                    (rs, row) -> rs.getLong(1), code);
            if (values.isEmpty()) throw new CodeGenerationException(Reason.COUNTER_NOT_INITIALIZED, "Missing counter");
            long next = values.getFirst();
            if (next == max + 1) throw new CodeGenerationException(Reason.CAPACITY_EXHAUSTED, "Exhausted");
            if (next < 1 || next > max) throw new CodeGenerationException(Reason.INVALID_NUMBER_STATE, "Invalid counter");
            jdbc.update("UPDATE " + table + " SET next_number = next_number + 1 WHERE scope_code = ?", code);
            return next;
        }
    }
}
