package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.config.persistence.ConstraintNamingIntegrator;
import com.misc.shafagh.config.persistence.DatabaseNamingStrategy;
import com.misc.shafagh.config.persistence.ShafaghOracleDialect;
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
        var bootstrap = new BootstrapServiceRegistryBuilder().applyIntegrator(new ConstraintNamingIntegrator()).build();
        var registry = new StandardServiceRegistryBuilder(bootstrap)
                .applySetting("hibernate.dialect", ShafaghOracleDialect.class.getName())
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .applySetting("hibernate.implicit_naming_strategy", new DatabaseNamingStrategy())
                .applySetting("jakarta.persistence.schema-generation.database.action", "none")
                .applySetting("jakarta.persistence.schema-generation.scripts.action", "create")
                .applySetting("jakarta.persistence.schema-generation.scripts.create-target", script.toString())
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
                assertTrue(ddl.contains("CONSTRAINT PK_" + table + " PRIMARY KEY"), table);
                assertTrue(ddl.contains("SEQ_" + table + "_ID"), table);
                assertTrue(ddl.contains("RENAME TO UDX_" + table + "_ID"), table);
            }
            long sequences = ddl.lines().filter(line -> line.startsWith("CREATE SEQUENCE")).count();
            assertEquals(20, sequences);
            ddl.lines().filter(line -> line.startsWith("CREATE SEQUENCE")).forEach(line -> {
                assertTrue(line.contains("NOCYCLE CACHE 50 NOORDER"), line);
            });
            assertTrue(ddl.contains("UK_PRODUCT_LOAN_CODE"));
            assertTrue(ddl.contains("FK_LOAN_PERSON_LOAN"));
            assertTrue(ddl.contains("CHK_LOAN_AMOUNT_NOT_NULL"));
            assertTrue(ddl.contains("CHK_LOAN_LOAN_STATUS"));
            assertFalse(ddl.contains("SYS_C"));
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
