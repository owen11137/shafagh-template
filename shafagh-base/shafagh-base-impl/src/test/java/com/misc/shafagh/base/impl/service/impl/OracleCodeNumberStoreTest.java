package com.misc.shafagh.base.impl.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.misc.shafagh.base.api.CodeGenerationException;
import com.misc.shafagh.base.api.CodeGenerationException.Reason;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Checks JDBC binding and error handling; real Oracle execution has a separate opt-in suite. */
class OracleCodeNumberStoreTest {

    private DataSource dataSource;
    private Connection connection;
    private CallableStatement statement;
    private OracleCodeNumberStoreImpl store;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(CallableStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(anyString())).thenReturn(statement);
        TransactionSynchronizationManager.bindResource(dataSource, new ConnectionHolder(connection));
        store = new OracleCodeNumberStoreImpl(dataSource,
                "SHFQ_BASE.SEQ_CUSTOMER_CUSTOMER_CODE", "SHFQ_BASE.BRANCH_CODE_COUNTER",
                "SHFQ_BASE.ACCOUNT_NUMBER_COUNTER", 5);
    }

    @AfterEach
    void cleanUp() {
        TransactionSynchronizationManager.unbindResourceIfPossible(dataSource);
    }

    @Test
    void accountUsesBoundScopeAndNumericOutputForOldValue() throws Exception {
        when(statement.getBigDecimal(2)).thenReturn(BigDecimal.valueOf(999_999));

        assertThat(store.allocateAccountNumber("56221000001")).isEqualTo(999_999);

        verify(statement).setString(1, "56221000001");
        verify(statement).registerOutParameter(2, Types.NUMERIC);
        verify(statement).setQueryTimeout(5);
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareCall(sql.capture());
        assertThat(sql.getValue()).contains("SHFQ_BASE.ACCOUNT_NUMBER_COUNTER", "BRANCH_CODE = ?",
                "BETWEEN 1 AND 999999", "RETURNING NEXT_NUMBER - 1", "SQL%ROWCOUNT = 0");
    }

    @Test
    void noUpdatedRowDistinguishesMissingCounter() throws Exception {
        ResultSet result = fallbackResult();
        when(result.next()).thenReturn(false);

        assertReason(Reason.COUNTER_NOT_INITIALIZED, () -> store.allocateAccountNumber("56221000001"));
    }

    @Test
    void noUpdatedRowDistinguishesExhaustedSentinel() throws Exception {
        ResultSet result = fallbackResult();
        when(result.next()).thenReturn(true, false);
        when(result.getBigDecimal(1)).thenReturn(BigDecimal.valueOf(1_000_000));

        assertReason(Reason.CAPACITY_EXHAUSTED, () -> store.allocateAccountNumber("56221000001"));
    }

    @Test
    void fractionalDatabaseOutputIsRejectedInsteadOfTruncated() throws Exception {
        when(statement.getBigDecimal(2)).thenReturn(new BigDecimal("1.5"));

        assertReason(Reason.INVALID_NUMBER_STATE, () -> store.allocateAccountNumber("56221000001"));
    }

    @Test
    void oracleSequenceExhaustionHasStableApiReason() throws Exception {
        Statement query = mock(Statement.class);
        when(connection.createStatement()).thenReturn(query);
        when(query.executeQuery(anyString())).thenThrow(new SQLException("sequence exceeds MAXVALUE", "72000", 8004));

        assertReason(Reason.CAPACITY_EXHAUSTED, () -> store.nextCustomerNumber());
    }

    @Test
    void rejectsWritesOutsideTheDataSourceTransaction() {
        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThatThrownBy(() -> store.allocateAccountNumber("56221000001"))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    void configuredObjectNamesCannotContainSqlFragments() {
        assertThatThrownBy(() -> new OracleCodeNumberStoreImpl(dataSource,
                "SEQ_CUSTOMER_CODE; DROP TABLE X", "BRANCH_CODE_COUNTER", "ACCOUNT_NUMBER_COUNTER", 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OracleCodeNumberStoreImpl(dataSource,
                "SEQ_CUSTOMER_CODE", "OTHER.COUNTER WHERE 1=1", "ACCOUNT_NUMBER_COUNTER", 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ResultSet fallbackResult() throws Exception {
        PreparedStatement query = mock(PreparedStatement.class);
        ResultSet result = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(query);
        when(query.executeQuery()).thenReturn(result);
        return result;
    }

    private static void assertReason(Reason reason, Runnable operation) {
        assertThatThrownBy(operation::run).isInstanceOfSatisfying(CodeGenerationException.class,
                exception -> assertThat(exception.getReason()).isEqualTo(reason));
    }
}
