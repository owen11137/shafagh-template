package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.misc.shafagh.config.persistence.TestSchemaReset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class TestSchemaResetGuardTest {
    @Test
    void refusesAConnectionUsingAnotherSchemaOwnerBeforeIssuingSql() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getUserName()).thenReturn("SYSTEM");
        assertThrows(IllegalStateException.class, () -> TestSchemaReset.reset(source, "SHFQ_LOAN"));
        verify(connection, never()).createStatement();
    }

    @Test
    void refusesAnySchemaOutsideTheAuthorizedFour() {
        DataSource source = mock(DataSource.class);
        assertThrows(IllegalArgumentException.class, () -> TestSchemaReset.reset(source, "OTHER"));
        verifyNoInteractions(source);
    }

    @Test
    void oracleDropsUseCascadeConstraintsAndPurgeAndIncludeOldSequences() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet tables = mock(ResultSet.class);
        ResultSet sequences = mock(ResultSet.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getUserName()).thenReturn("SHFQ_LOAN");
        when(metadata.getDatabaseProductName()).thenReturn("Oracle");
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(startsWith("select table_name"))).thenReturn(tables);
        when(statement.executeQuery("select sequence_name from user_sequences")).thenReturn(sequences);
        when(tables.next()).thenReturn(true, false);
        when(tables.getString(1)).thenReturn("LOAN");
        when(sequences.next()).thenReturn(true, false);
        when(sequences.getString(1)).thenReturn("LOAN_ID_SEQ");
        TestSchemaReset.reset(source, "SHFQ_LOAN");
        verify(statement).execute("purge recyclebin");
        verify(statement).execute("drop table \"SHFQ_LOAN\".\"LOAN\" cascade constraints purge");
        verify(statement).execute("drop sequence \"SHFQ_LOAN\".\"LOAN_ID_SEQ\"");
    }
}
