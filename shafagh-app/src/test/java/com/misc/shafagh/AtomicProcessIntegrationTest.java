package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.base.api.dto.CityDto;
import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO;
import com.misc.shafagh.dpst.impl.entity.DepositType;
import com.misc.shafagh.dpst.impl.service.api.ProductDepositService;
import com.misc.shafagh.orchestration.service.api.AtomicProcessExecutor;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest(properties = "shafagh.fund-id=73")
class AtomicProcessIntegrationTest {
    @org.springframework.test.context.DynamicPropertySource
    static void database(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:atomic_process;MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SHFQ_BASE\\;CREATE SCHEMA IF NOT EXISTS SHFQ_CIF\\;CREATE SCHEMA IF NOT EXISTS SHFQ_DPST\\;CREATE SCHEMA IF NOT EXISTS SHFQ_LOAN");
    }
    @Autowired private ApplicationContext context;
    @Autowired private AtomicProcessExecutor processes;
    @Autowired private CityService cities;
    @Autowired private ProductDepositService products;
    @Autowired private DataSource source;

    @Test void allModulesShareTheSamePersistenceAndTransactionResources() {
        for (String module : new String[]{"base", "cif", "dpst", "loan"}) {
            assertSame(source, context.getBean(module + "DataSource"));
            assertSame(context.getBean("entityManagerFactory"), context.getBean(module + "EntityManagerFactory"));
            assertSame(context.getBean("transactionManager"), context.getBean(module + "TransactionManager"));
        }
        assertSame(context.getBean("transactionManager"), context.getBean("appTransactionManager"));
    }

    @Test void successfulWorkflowCommitsBothSchemas() throws Exception {
        String marker = UUID.randomUUID().toString();
        Long cityId = processes.execute(() -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            Long id = createCity(marker);
            products.create(product(marker));
            assertRows(marker, 1);
            return id;
        });
        assertNotNull(cityId);
        assertRows(marker, 1);
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test void checkedFailureRollsBackAlreadyFlushedChangesInBothSchemas() {
        String marker = UUID.randomUUID().toString();
        assertThrows(Exception.class, () -> processes.execute(() -> {
            createCity(marker);
            products.create(product(marker));
            assertRows(marker, 1);
            throw new Exception("Second module failed");
        }));
        assertRows(marker, 0);
    }

    @Test void runtimeFailureRollsBackAlreadyFlushedChangesInBothSchemas() {
        String marker = UUID.randomUUID().toString();
        assertThrows(IllegalStateException.class, () -> processes.execute(() -> {
            createCity(marker);
            products.create(product(marker));
            assertRows(marker, 1);
            throw new IllegalStateException("Workflow failed");
        }));
        assertRows(marker, 0);
    }

    private Long createCity(String marker) {
        CityDto city = new CityDto();
        city.setName(marker);
        city.setParentId(0L);
        return cities.save(city).getId();
    }

    private ProductDepositReqDTO product(String marker) {
        ProductDepositReqDTO request = new ProductDepositReqDTO();
        request.setCode(marker);
        request.setStatus(ProductStatus.ACTIVE);
        request.setDepositType(DepositType.DEPOSIT1);
        return request;
    }

    private void assertRows(String marker, int count) {
        JdbcTemplate jdbc = new JdbcTemplate(source);
        assertEquals(count, jdbc.queryForObject("select count(*) from SHFQ_BASE.CITY where NAME=?", Integer.class, marker));
        assertEquals(count, jdbc.queryForObject("select count(*) from SHFQ_DPST.PRODUCT_DEPOSIT where CODE=?", Integer.class, marker));
    }
}
