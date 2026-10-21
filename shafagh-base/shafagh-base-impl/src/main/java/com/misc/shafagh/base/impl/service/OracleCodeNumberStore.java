package com.misc.shafagh.base.impl.service;

import com.misc.shafagh.base.api.CodeGenerationException;
import com.misc.shafagh.base.api.CodeGenerationException.Reason;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.IllegalTransactionStateException;

/** Oracle-specific atomic counter updates; no DDL or automatic counter reset. */
@Repository
class OracleCodeNumberStore implements CodeNumberStore {

    private static final Pattern IDENTIFIER = Pattern.compile(
            "[A-Za-z][A-Za-z0-9_$#]{0,127}(\\.[A-Za-z][A-Za-z0-9_$#]{0,127})?");

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final String customerSequence;
    private final Counter branchCounter;
    private final Counter accountCounter;

    OracleCodeNumberStore(
            JdbcTemplate jdbcTemplate,
            @Value("${app.code-sequences.customer:SHFQ_BASE.CUSTOMER_CODE_SEQ}") String customerSequence,
            @Value("${app.code-counters.branch:SHFQ_BASE.BRANCH_CODE_COUNTER}") String branchTable,
            @Value("${app.code-counters.account:SHFQ_BASE.ACCOUNT_NUMBER_COUNTER}") String accountTable,
            @Value("${app.code-allocation.timeout-seconds:5}") int timeoutSeconds) {
        if (timeoutSeconds < 1) {
            throw new IllegalArgumentException("مهلت تخصیص شماره باید مثبت باشد");
        }
        this.dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        // Keep statement timeouts local; never change the application's shared template.
        this.jdbc = new JdbcTemplate(dataSource);
        this.jdbc.setQueryTimeout(timeoutSeconds);
        this.customerSequence = validateIdentifier(customerSequence);
        this.branchCounter = new Counter(validateIdentifier(branchTable), "FUND_CODE", MAX_BRANCH_NUMBER);
        this.accountCounter = new Counter(validateIdentifier(accountTable), "BRANCH_CODE", MAX_ACCOUNT_NUMBER);
        if (branchTable.equalsIgnoreCase(accountTable)) {
            throw new IllegalArgumentException("جدول شمارنده شعبه و حساب باید مجزا باشند");
        }
    }

    @Override
    public long nextCustomerNumber() {
        try {
            BigDecimal value = jdbc.queryForObject(
                    "SELECT " + customerSequence + ".NEXTVAL FROM DUAL", BigDecimal.class);
            if (value == null) {
                throw invalidState("مقدار sequence مشتری تهی است");
            }
            long number = exactLong(value);
            if (number < 1) {
                throw invalidState("مقدار sequence مشتری باید مثبت باشد");
            }
            if (number > MAX_CUSTOMER_NUMBER) {
                throw exhausted("ظرفیت کد مشتری پر شده است");
            }
            return number;
        } catch (DataAccessException exception) {
            if (hasOracleError(exception, 8004)) {
                throw new CodeGenerationException(Reason.CAPACITY_EXHAUSTED,
                        "ظرفیت کد مشتری پر شده است", exception);
            }
            throw exception;
        }
    }

    @Override
    public long allocateBranchNumber(String fundCode) {
        return allocate(branchCounter, fundCode);
    }

    @Override
    public long allocateAccountNumber(String branchCode) {
        return allocate(accountCounter, branchCode);
    }

    @Override
    public void initializeBranchCounter(String fundCode) {
        initialize(branchCounter, fundCode);
    }

    @Override
    public void initializeAccountCounter(String branchCode) {
        initialize(accountCounter, branchCode);
    }

    private long allocate(Counter counter, String scopeCode) {
        // RETURNING is undefined for zero updated rows; explicitly null the local
        // output in that case before binding the single JDBC OUT parameter.
        String sql = "DECLARE allocated NUMBER; BEGIN UPDATE " + counter.table()
                + " SET NEXT_NUMBER = NEXT_NUMBER + 1 WHERE " + counter.keyColumn() + " = ?"
                + " AND NEXT_NUMBER BETWEEN 1 AND " + counter.maximum()
                + " RETURNING NEXT_NUMBER - 1 INTO allocated;"
                + " IF SQL%ROWCOUNT = 0 THEN allocated := NULL; END IF; ? := allocated; END;";
        BigDecimal allocated = jdbc.execute((CallableStatementCreator) connection -> {
            requireTransactionalConnection(connection);
            var statement = connection.prepareCall(sql);
            statement.setString(1, scopeCode);
            statement.registerOutParameter(2, Types.NUMERIC);
            return statement;
        }, statement -> {
            statement.execute();
            return statement.getBigDecimal(2);
        });
        if (allocated == null) {
            throwAllocationFailure(counter, scopeCode);
        }
        long number = exactLong(allocated);
        if (number < 1 || number > counter.maximum()) {
            throw invalidState("مقدار تخصیص‌یافته خارج از محدوده است");
        }
        return number;
    }

    private void initialize(Counter counter, String scopeCode) {
        String sql = "INSERT INTO " + counter.table() + " (" + counter.keyColumn() + ", NEXT_NUMBER) VALUES (?, 1)";
        try {
            Integer affected = jdbc.execute((PreparedStatementCreator) connection -> {
                requireTransactionalConnection(connection);
                var statement = connection.prepareStatement(sql);
                statement.setString(1, scopeCode);
                return statement;
            }, statement -> statement.executeUpdate());
            if (affected == null || affected != 1) {
                throw invalidState("ایجاد رکورد شمارنده ناموفق بود");
            }
        } catch (DuplicateKeyException exception) {
            throw new CodeGenerationException(Reason.COUNTER_ALREADY_INITIALIZED,
                    "شمارنده قبلاً ایجاد شده است و بازنشانی نمی‌شود", exception);
        }
    }

    private void throwAllocationFailure(Counter counter, String scopeCode) {
        List<BigDecimal> values = jdbc.query(
                "SELECT NEXT_NUMBER FROM " + counter.table() + " WHERE " + counter.keyColumn() + " = ?",
                (result, row) -> result.getBigDecimal(1), scopeCode);
        if (values.isEmpty()) {
            throw new CodeGenerationException(Reason.COUNTER_NOT_INITIALIZED, "رکورد شمارنده ایجاد نشده است");
        }
        if (values.size() == 1 && values.getFirst() != null
                && values.getFirst().compareTo(BigDecimal.valueOf(counter.maximum() + 1)) == 0) {
            throw exhausted("ظرفیت شماره‌گذاری پر شده است");
        }
        throw invalidState("وضعیت شمارنده معتبر نیست");
    }

    private void requireTransactionalConnection(Connection connection) {
        if (!DataSourceUtils.isConnectionTransactional(connection, dataSource)) {
            throw new IllegalTransactionStateException(
                    "مدیر تراکنش باید همان DataSource تخصیص شماره را مدیریت کند");
        }
    }

    private static long exactLong(BigDecimal value) {
        try {
            return value.longValueExact();
        } catch (ArithmeticException exception) {
            throw new CodeGenerationException(Reason.INVALID_NUMBER_STATE,
                    "مقدار شمارنده باید عدد صحیح معتبر باشد", exception);
        }
    }

    private static String validateIdentifier(String value) {
        if (value == null || !IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("نام آبجکت دیتابیس معتبر نیست");
        }
        return value;
    }

    private static boolean hasOracleError(Throwable exception, int errorCode) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException && sqlException.getErrorCode() == errorCode) {
                return true;
            }
        }
        return false;
    }

    private static CodeGenerationException invalidState(String message) {
        return new CodeGenerationException(Reason.INVALID_NUMBER_STATE, message);
    }

    private static CodeGenerationException exhausted(String message) {
        return new CodeGenerationException(Reason.CAPACITY_EXHAUSTED, message);
    }

    private record Counter(String table, String keyColumn, long maximum) {
    }
}
