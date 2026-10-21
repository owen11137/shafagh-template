package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.misc.shafagh.base.api.CodeGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Checks API discovery and JDBC participation in the application's actual JPA transaction. */
@SpringBootTest(properties = {
        "app.code-counters.branch=SHFQ_BASE.WIRING_BRANCH_COUNTER",
        "app.code-counters.account=SHFQ_BASE.WIRING_ACCOUNT_COUNTER"
})
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "^$")
class CodeGeneratorWiringTest {

    @Autowired private CodeGeneratorService generator;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void counterInitializationParticipatesInTheJpaRegistrationTransaction() {
        assertInstanceOf(JpaTransactionManager.class, transactionManager);
        jdbc.execute("CREATE TABLE SHFQ_BASE.WIRING_BRANCH_COUNTER "
                + "(FUND_CODE VARCHAR(6) PRIMARY KEY, NEXT_NUMBER BIGINT NOT NULL)");
        jdbc.execute("CREATE TABLE SHFQ_BASE.WIRING_ACCOUNT_COUNTER "
                + "(BRANCH_CODE VARCHAR(11) PRIMARY KEY, NEXT_NUMBER BIGINT NOT NULL)");
        try {
            TransactionTemplate registration = new TransactionTemplate(transactionManager);
            registration.executeWithoutResult(status -> {
                generator.initializeBranchCounter("562210");
                generator.initializeAccountCounter("56221000001");
                status.setRollbackOnly();
            });
            assertEquals(0L, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM SHFQ_BASE.WIRING_BRANCH_COUNTER", Long.class));
            assertEquals(0L, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM SHFQ_BASE.WIRING_ACCOUNT_COUNTER", Long.class));

            registration.executeWithoutResult(status -> generator.initializeBranchCounter("562210"));
            assertEquals(1L, jdbc.queryForObject(
                    "SELECT NEXT_NUMBER FROM SHFQ_BASE.WIRING_BRANCH_COUNTER WHERE FUND_CODE = '562210'", Long.class));
        } finally {
            jdbc.execute("DROP TABLE SHFQ_BASE.WIRING_ACCOUNT_COUNTER");
            jdbc.execute("DROP TABLE SHFQ_BASE.WIRING_BRANCH_COUNTER");
        }
    }
}
