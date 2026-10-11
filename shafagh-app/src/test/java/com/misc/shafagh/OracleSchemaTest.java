package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.misc.shafagh.base.impl.BaseSchema;
import com.misc.shafagh.cif.impl.CifSchema;
import com.misc.shafagh.dpst.impl.DpstSchema;
import com.misc.shafagh.loan.impl.LoanSchema;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * ساختار واقعی Oracle. فقط با -Dtest.profile=oracle اجرا می‌شود.
 * (تطابق Entityها با ستون‌های جدول‌ها هم هنگام بالا آمدن برنامه با ddl-auto=validate بررسی می‌شود.)
 */
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = ".*oracle.*")
class OracleSchemaTest extends AbstractScenarioSupport {

    private static final List<SchemaObject> TABLES = List.of(
            new SchemaObject(BaseSchema.NAME, "SHFQ_BASE_CURRENCY"),
            new SchemaObject(CifSchema.NAME, "SHFQ_CIF_PERSON"),
            new SchemaObject(DpstSchema.NAME, "SHFQ_DPST_DEPOSIT"),
            new SchemaObject(DpstSchema.NAME, "PRODUCT_DEPOSIT"),
            new SchemaObject(LoanSchema.NAME, "SHFQ_LOAN_LOAN"));

    private static final List<SchemaObject> SEQUENCES = List.of(
            new SchemaObject(CifSchema.NAME, "SHFQ_CIF_PERSON_SEQ"),
            new SchemaObject(DpstSchema.NAME, "SHFQ_DPST_DEPOSIT_SEQ"),
            new SchemaObject(DpstSchema.NAME, "PRODUCT_DEPOSIT_ID_SEQ"),
            new SchemaObject(LoanSchema.NAME, "SHFQ_LOAN_LOAN_SEQ"));

    private static final Set<String> SCHEMAS = TABLES.stream()
            .map(SchemaObject::schema)
            .collect(Collectors.toUnmodifiableSet());

    @Test
    void everyModuleTableLivesInTheSchemaOfItsOwnModule() {
        TABLES.forEach(table -> {
            int count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME = ?",
                    Integer.class, table.schema(), table.name());
            assertEquals(1, count, "table " + table.name() + " must exist in schema " + table.schema()
                    + " (and be visible to the connection user: check scripts/03-grants.sql)");
        });
    }

    @Test
    void everyModuleSequenceLivesInTheSchemaOfItsOwnModule() {
        SEQUENCES.forEach(sequence -> {
            int count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = ? AND SEQUENCE_NAME = ?",
                    Integer.class, sequence.schema(), sequence.name());
            assertEquals(1, count, "sequence " + sequence.name() + " must exist in schema " + sequence.schema());
        });
    }

    @Test
    void noForeignKeyCrossesSchemas_modulesAreLinkedOnlyThroughServices() {
        String owners = "'" + String.join("','", SCHEMAS) + "'";
        int crossing = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ALL_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'R' AND OWNER IN (" + owners + ") "
                        + "AND R_OWNER <> OWNER", Integer.class);
        assertEquals(0, crossing, "foreign keys between module schemas are not allowed");
    }

    @Test
    void connectionUserCanWriteToAllSchemas_andRowsLandInTheOwningSchema() {
        var person = newPerson();
        var deposit = newDeposit(person, "100");

        assertEquals(1, rowCount(T_PERSON, person.id()));
        assertEquals(1, rowCount(T_DEPOSIT, deposit.id()));
    }

    private record SchemaObject(String schema, String name) {}
}
