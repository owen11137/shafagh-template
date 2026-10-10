package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.misc.shafagh.base.impl.BaseSchema;
import com.misc.shafagh.cif.impl.CifSchema;
import com.misc.shafagh.dpst.impl.DpstSchema;
import com.misc.shafagh.loan.impl.LoanSchema;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * ساختار واقعی Oracle. فقط با -Dtest.profile=oracle اجرا می‌شود.
 * (تطابق Entityها با ستون‌های جدول‌ها هم هنگام بالا آمدن برنامه با ddl-auto=validate بررسی می‌شود.)
 */
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = ".*oracle.*")
class OracleSchemaTest extends AbstractScenarioSupport {

    private static final Map<String, String> TABLES = Map.of(
            BaseSchema.NAME, "SHFQ_BASE_CURRENCY",
            CifSchema.NAME, "SHFQ_CIF_PERSON",
            DpstSchema.NAME, "SHFQ_DPST_DEPOSIT",
            LoanSchema.NAME, "SHFQ_LOAN_LOAN");

    private static final Map<String, String> SEQUENCES = Map.of(
            CifSchema.NAME, "SHFQ_CIF_PERSON_SEQ",
            DpstSchema.NAME, "SHFQ_DPST_DEPOSIT_SEQ",
            LoanSchema.NAME, "SHFQ_LOAN_LOAN_SEQ");

    @Test
    void everyModuleTableLivesInTheSchemaOfItsOwnModule() {
        TABLES.forEach((schema, table) -> {
            int count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME = ?", Integer.class, schema, table);
            assertEquals(1, count, "table " + table + " must exist in schema " + schema
                    + " (and be visible to the connection user: check scripts/03-grants.sql)");
        });
    }

    @Test
    void everyModuleSequenceLivesInTheSchemaOfItsOwnModule() {
        SEQUENCES.forEach((schema, sequence) -> {
            int count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = ? AND SEQUENCE_NAME = ?",
                    Integer.class, schema, sequence);
            assertEquals(1, count, "sequence " + sequence + " must exist in schema " + schema);
        });
    }

    @Test
    void noForeignKeyCrossesSchemas_modulesAreLinkedOnlyThroughServices() {
        String owners = "'" + String.join("','", TABLES.keySet()) + "'";
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
}
