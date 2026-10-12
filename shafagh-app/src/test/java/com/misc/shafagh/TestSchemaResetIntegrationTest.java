package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.base.api.dto.CityDto;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("reset")
class TestSchemaResetIntegrationTest {
    @Autowired private Map<String, DataSource> sources;
    @Autowired private CityService cities;

    @DynamicPropertySource
    static void seedOldSchemas(DynamicPropertyRegistry registry) throws Exception {
        for (String module : List.of("base", "cif", "dpst", "loan")) {
            String schema = "SHFQ_" + module.toUpperCase();
            String username = "shfq_" + module;
            String url = "jdbc:h2:mem:reset_" + module + ";MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS " + schema;
            try (var connection = DriverManager.getConnection(url, username, username); var statement = connection.createStatement()) {
                statement.execute("create table " + schema + ".LEGACY_PARENT(ID bigint primary key)");
                statement.execute("insert into " + schema + ".LEGACY_PARENT values(42)");
                statement.execute("create table " + schema + ".LEGACY_CHILD(ID bigint, PARENT_ID bigint references " + schema + ".LEGACY_PARENT(ID))");
                statement.execute("create sequence " + schema + ".LEGACY_SEQUENCE start with 99");
                statement.execute("create schema UNRELATED");
                statement.execute("create table UNRELATED.KEEP_DATA(ID bigint)");
                statement.execute("insert into UNRELATED.KEEP_DATA values(77)");
            }
            String prefix = "shafagh.persistence." + module + ".";
            registry.add(prefix + "url", () -> url);
            registry.add(prefix + "username", () -> username);
            registry.add(prefix + "password", () -> username);
            registry.add(prefix + "driver-class-name", () -> "org.h2.Driver");
        }
    }

    @Test
    void removesOldObjectsAndRebuildsCurrentTablesWithoutTouchingOtherSchemas() {
        int totalTables = 0;
        for (String module : List.of("base", "cif", "dpst", "loan")) {
            String schema = "SHFQ_" + module.toUpperCase();
            var jdbc = new JdbcTemplate(sources.get(module + "DataSource"));
            assertEquals(0, jdbc.queryForObject("select count(*) from information_schema.tables where table_schema=? and table_name like 'LEGACY_%'", Integer.class, schema));
            assertEquals(0, jdbc.queryForObject("select count(*) from information_schema.sequences where sequence_schema=? and sequence_name='LEGACY_SEQUENCE'", Integer.class, schema));
            assertEquals(77L, jdbc.queryForObject("select ID from UNRELATED.KEEP_DATA", Long.class));
            var tables = jdbc.queryForList("select TABLE_NAME from information_schema.tables where TABLE_SCHEMA=?", String.class, schema);
            totalTables += tables.size();
            for (String table : tables) {
                assertEquals(0L, jdbc.queryForObject("select count(*) from " + schema + "." + table, Long.class));
                assertEquals(1, jdbc.queryForObject("select count(*) from information_schema.table_constraints where constraint_schema=? and constraint_name=?", Integer.class, schema, "PK_" + table));
                assertEquals(1, jdbc.queryForObject("select count(*) from information_schema.sequences where sequence_schema=? and sequence_name=?", Integer.class, schema, "SEQ_" + table + "_ID"));
            }
        }
        assertEquals(20, totalTables);
        var city = new CityDto(); city.setName("تهران"); city.setParentId(0L);
        CityDto saved = cities.save(city);
        assertEquals(1L, saved.getId());
        assertEquals("تهران", cities.getById(saved.getId()).getName());
    }
}
