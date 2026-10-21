package com.misc.shafagh.base.impl.service;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.base.api.CodeGenerationException;
import com.misc.shafagh.base.api.CodeGenerationException.Reason;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Opt-in real Oracle verification. Credentials must identify a disposable test schema
 * with CREATE TABLE/SEQUENCE permissions. Only uniquely named objects created here are dropped.
 */
@EnabledIfEnvironmentVariable(named = "ORACLE_TEST_URL", matches = ".+")
class OracleCodeNumberStoreIntegrationTest {
    private JdbcTemplate jdbc;
    private TransactionTemplate registration;
    private CodeGeneratorServiceImpl service;
    private OracleCodeNumberStore store;
    private String branches;
    private String accounts;
    private final List<String> cleanup = new ArrayList<>();

    @BeforeEach
    void setup() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl(System.getenv("ORACLE_TEST_URL"));
        dataSource.setUsername(Objects.requireNonNull(System.getenv("ORACLE_TEST_USERNAME"), "ORACLE_TEST_USERNAME required"));
        dataSource.setPassword(Objects.requireNonNull(System.getenv("ORACLE_TEST_PASSWORD"), "ORACLE_TEST_PASSWORD required"));
        jdbc = new JdbcTemplate(dataSource);
        jdbc.setQueryTimeout(15);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(java.util.Locale.ROOT);
        branches = "CGT_B_" + suffix;
        accounts = "CGT_A_" + suffix;
        String sequence = "CGT_S_" + suffix;
        jdbc.execute("CREATE TABLE " + branches
                + " (FUND_CODE VARCHAR2(6 CHAR) PRIMARY KEY, NEXT_NUMBER NUMBER(6,0) NOT NULL CHECK (NEXT_NUMBER BETWEEN 1 AND 100000))");
        cleanup.add("DROP TABLE " + branches + " PURGE");
        jdbc.execute("CREATE TABLE " + accounts
                + " (BRANCH_CODE VARCHAR2(11 CHAR) PRIMARY KEY, NEXT_NUMBER NUMBER(7,0) NOT NULL CHECK (NEXT_NUMBER BETWEEN 1 AND 1000000))");
        cleanup.add("DROP TABLE " + accounts + " PURGE");
        // A small maximum exercises Oracle's actual ORA-08004 exhaustion path.
        jdbc.execute("CREATE SEQUENCE " + sequence + " START WITH 1 INCREMENT BY 1 MAXVALUE 2 NOCYCLE NOCACHE");
        cleanup.add("DROP SEQUENCE " + sequence);
        JdbcTransactionManager manager = new JdbcTransactionManager(dataSource);
        registration = new TransactionTemplate(manager);
        store = new OracleCodeNumberStore(jdbc, sequence, branches, accounts, 15);
        service = new CodeGeneratorServiceImpl(store, manager, 15);
    }

    @AfterEach
    void cleanup() {
        for (int i = cleanup.size() - 1; i >= 0; i--) jdbc.execute(cleanup.get(i));
    }

    @Test
    void realReturningHandlesConcurrencyRollbackAndLastValidNumber() throws Exception {
        registration.executeWithoutResult(s -> {
            service.initializeBranchCounter("562210");
            service.initializeAccountCounter("56221000001");
        });
        registration.executeWithoutResult(s -> {
            assertEquals("56221000001", service.generateBranchCode("562210"));
            s.setRollbackOnly();
        });
        assertEquals("56221000001", registration.execute(s -> service.generateBranchCode("562210")));
        String reserved = registration.execute(s -> {
            jdbc.queryForObject("SELECT COUNT(*) FROM " + accounts, Long.class);
            String number = service.generateAccountNumber("56221000001");
            s.setRollbackOnly();
            return number;
        });
        assertEquals("000001", reserved.substring(11, 17));
        assertEquals("000002", service.generateAccountNumber("56221000001").substring(11, 17));

        var executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        try {
            var futures = new ArrayList<java.util.concurrent.Future<List<String>>>();
            for (int t = 0; t < 4; t++) {
                futures.add(executor.submit(() -> {
                    assertTrue(start.await(10, TimeUnit.SECONDS));
                    List<String> results = new ArrayList<>();
                    for (int i = 0; i < 10; i++) results.add(service.generateAccountNumber("56221000001"));
                    return results;
                }));
            }
            start.countDown();
            var numbers = new HashSet<String>();
            for (var future : futures)
                for (String number : future.get(60, TimeUnit.SECONDS)) assertTrue(numbers.add(number));
            assertEquals(40, numbers.size());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
        assertEquals(43L, jdbc.queryForObject("SELECT NEXT_NUMBER FROM " + accounts, Long.class));
        jdbc.update("UPDATE " + accounts + " SET NEXT_NUMBER = 999999");
        assertEquals("999999", service.generateAccountNumber("56221000001").substring(11, 17));
        assertEquals(1000000L, jdbc.queryForObject("SELECT NEXT_NUMBER FROM " + accounts, Long.class));
        assertReason(Reason.CAPACITY_EXHAUSTED, () -> service.generateAccountNumber("56221000001"));
        jdbc.update("UPDATE " + branches + " SET NEXT_NUMBER = 99999");
        assertEquals("56221099999", registration.execute(s -> service.generateBranchCode("562210")));
        assertReason(Reason.CAPACITY_EXHAUSTED,
                () -> registration.execute(s -> service.generateBranchCode("562210")));
    }

    @Test
    void realOracleDistinguishesMissingDuplicateAndExhaustedSequence() {
        assertThrows(IllegalTransactionStateException.class, () -> store.initializeBranchCounter("562210"));
        assertReason(Reason.COUNTER_NOT_INITIALIZED, () -> service.generateAccountNumber("56221000001"));
        registration.executeWithoutResult(s -> service.initializeAccountCounter("56221000001"));
        service.generateAccountNumber("56221000001");
        assertReason(Reason.COUNTER_ALREADY_INITIALIZED,
                () -> registration.executeWithoutResult(s -> service.initializeAccountCounter("56221000001")));
        assertEquals("000002", service.generateAccountNumber("56221000001").substring(11, 17));
        assertEquals("56221000000001", service.generateCustomerCode("562210"));
        assertEquals("56221100000002", service.generateCustomerCode("562211"));
        assertReason(Reason.CAPACITY_EXHAUSTED, () -> service.generateCustomerCode("562210"));
    }

    private static void assertReason(Reason expected, org.junit.jupiter.api.function.Executable action) {
        assertEquals(expected, assertThrows(CodeGenerationException.class, action).getReason());
    }
}
