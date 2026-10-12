package com.misc.shafagh.config.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.misc.shafagh.common.SchemaName;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Explicitly requested reset of dedicated development/test schema tables and sequences. */
public final class TestSchemaReset {
    private static final Logger LOG = LoggerFactory.getLogger(TestSchemaReset.class);
    private TestSchemaReset() {}

    public static void reset(DataSource source, String expectedOwner) {
        if (!Set.of(SchemaName.BASE, SchemaName.CIF, SchemaName.DP, SchemaName.LN)
                .contains(expectedOwner.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Reset is restricted to the four Shafagh test schemas");
        }
        try (Connection connection = source.getConnection()) {
            String user = connection.getMetaData().getUserName();
            if (!expectedOwner.equalsIgnoreCase(user)) {
                throw new IllegalStateException("Schema reset requires its own owner account: expected "
                        + expectedOwner + ", connected as " + user);
            }
            String database = connection.getMetaData().getDatabaseProductName();
            boolean oracle = "Oracle".equalsIgnoreCase(database);
            boolean h2 = "H2".equalsIgnoreCase(database);
            if (!oracle && !h2) throw new IllegalStateException("Schema reset supports Oracle and test H2 only");
            String owner = expectedOwner.toUpperCase(Locale.ROOT);
            // Oracle recycle-bin objects cannot be dropped with ordinary DROP TABLE.
            if (oracle) {
                try (var statement = connection.createStatement()) {
                    statement.execute("purge recyclebin");
                }
            }
            String tablesQuery = oracle
                    ? "select table_name from user_tables where nested='NO' and secondary='N' and (iot_type is null or iot_type='IOT')"
                    : "select table_name from information_schema.tables where table_schema='" + owner + "' and table_type='BASE TABLE'";
            String sequencesQuery = oracle ? "select sequence_name from user_sequences"
                    : "select sequence_name from information_schema.sequences where sequence_schema='" + owner + "'";
            List<String> tables = names(connection, tablesQuery);
            List<String> sequences = names(connection, sequencesQuery);
            LOG.warn("Resetting test schema {}: removing {} tables and {} sequences", owner, tables.size(), sequences.size());
            try (var statement = connection.createStatement()) {
                for (String table : tables) {
                    statement.execute("drop table " + quote(owner) + "." + quote(table)
                            + (oracle ? " cascade constraints purge" : " cascade"));
                }
                for (String sequence : sequences) {
                    statement.execute("drop sequence " + quote(owner) + "." + quote(sequence));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to reset test schema " + expectedOwner, exception);
        }
    }

    private static List<String> names(Connection connection, String query) throws SQLException {
        List<String> names = new ArrayList<>();
        try (var statement = connection.createStatement(); var rows = statement.executeQuery(query)) {
            while (rows.next()) names.add(rows.getString(1));
        }
        return names;
    }

    private static String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
