package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.config.ShafaghOracleDialect;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Generates real Hibernate Oracle DDL without connecting to an Oracle server. */
class OracleNamingDdlTest {
    @TempDir Path directory;

    @Test
    void oracleDdlNamesKeysChecksIndexesAndUsesExplicitSequenceOptions() throws Exception {
        Path script = directory.resolve("oracle-schema.sql");
        Path dropScript = directory.resolve("oracle-drop.sql");
        var bootstrap = new BootstrapServiceRegistryBuilder().build();
        var registry = new StandardServiceRegistryBuilder(bootstrap)
                .applySetting("hibernate.dialect", ShafaghOracleDialect.class.getName())
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .applySetting("jakarta.persistence.schema-generation.database.action", "none")
                .applySetting("jakarta.persistence.schema-generation.scripts.action", "drop-and-create")
                .applySetting("jakarta.persistence.schema-generation.scripts.create-target", script.toString())
                .applySetting("jakarta.persistence.schema-generation.scripts.drop-target", dropScript.toString())
                .build();
        try {
            var sources = new MetadataSources(registry);
            var classes = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests()).importPackages("com.misc.shafagh");
            for (var type : classes) {
                if (type.isAnnotatedWith(jakarta.persistence.Entity.class)) sources.addAnnotatedClass(type.reflect());
            }
            var metadata = sources.buildMetadata();
            try (var factory = metadata.buildSessionFactory()) {
                assertTrue(factory.isOpen());
            }
            String ddl = Files.readString(script).toUpperCase(Locale.ROOT);
            Files.writeString(Path.of("target/oracle-naming-ddl.sql"), ddl);
            for (var type : classes) {
                if (!type.isAnnotatedWith(jakarta.persistence.Entity.class)) continue;
                Class<?> entity = type.reflect();
                String table = entity.getAnnotation(jakarta.persistence.Table.class).name();
                assertTrue(ddl.contains("PRIMARY KEY (ID)"), table);
                assertTrue(ddl.contains("SEQ_" + table + "_ID"), table);
            }
            long sequences = ddl.lines().filter(line -> line.startsWith("CREATE SEQUENCE")).count();
            assertEquals(20, sequences);
            ddl.lines().filter(line -> line.startsWith("CREATE SEQUENCE")).forEach(line -> {
                assertTrue(line.contains("NOCYCLE CACHE 50 NOORDER"), line);
            });
            assertTrue(ddl.contains("UK_PRODUCT_LOAN_CODE"));
            assertTrue(ddl.contains("CONSTRAINT UK_CUSTOMER_FUND_ID_IDENTIFIER UNIQUE (FUND_ID, IDENTIFIER)"));
            assertFalse(ddl.contains("ALTER INDEX"));
            assertFalse(ddl.contains("RENAME TO"));
            assertTrue(ddl.contains("CONSTRAINT UK_BRANCH_ADDRESS_BRANCH_ID UNIQUE"));
            assertTrue(ddl.contains("CONSTRAINT UK_FUND_ADDRESS_FUND_ID UNIQUE"));
            assertTrue(ddl.contains("FK_BRANCH_FUND"));
            assertTrue(ddl.contains("FK_LOAN_PERSON_LOAN"));
            assertFalse(ddl.contains("SYS_C"));
            // Verify every physical column, including inherited audit/tenant and join columns.
            int commentedColumns = 0;
            for (var namespace : metadata.getDatabase().getNamespaces()) {
                for (var table : namespace.getTables()) {
                    if (!table.isPhysicalTable()) continue;
                    assertNotNull(table.getComment(), table.getName());
                    assertFalse(table.getComment().isBlank(), table.getName());
                    for (var column : table.getColumns()) {
                        assertNotNull(column.getComment(), table.getName() + "." + column.getName());
                        assertFalse(column.getComment().isBlank(), table.getName() + "." + column.getName());
                        String statement = "COMMENT ON COLUMN " + table.getSchema() + "." + table.getName() + "." + column.getName() + " IS '";
                        assertTrue(ddl.contains(statement.toUpperCase(Locale.ROOT)), statement);
                        assertTrue(ddl.contains(column.getComment().replace("'", "''").toUpperCase(Locale.ROOT)), column.getComment());
                        commentedColumns++;
                    }
                }
            }
            assertTrue(commentedColumns > 100, "All physical columns must have Oracle comments");
            String drops = Files.readString(dropScript).toUpperCase(Locale.ROOT);
            assertEquals(20, drops.lines().filter(line -> line.contains("DROP SEQUENCE")).count());
            assertEquals(20, drops.lines().filter(line -> line.contains("DROP TABLE")).count());
            drops.lines().filter(line -> !line.isBlank()).forEach(line -> {
                assertTrue(line.startsWith("BEGIN EXECUTE IMMEDIATE"), line);
                assertTrue(line.contains("THEN RAISE; END IF; END;"), line);
                if (line.contains("DROP SEQUENCE")) assertTrue(line.contains("SQLCODE NOT IN (-2289)"), line);
                else if (line.contains("DROP TABLE")) assertTrue(line.contains("SQLCODE NOT IN (-942)"), line);
                else assertTrue(line.contains("SQLCODE NOT IN (-942, -2443)"), line);
            });
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
