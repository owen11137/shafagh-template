package com.misc.shafagh.dpst.impl.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.misc.shafagh.common.product.ProductStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "shafagh.audit.actor=test-operator")
@Transactional
class DepositPersistenceTest {

    @PersistenceContext private EntityManager entityManager;
    @Value("${shafagh.fund-id:1}") private long fundId;

    @Test
    void depositModelsPersistWithTenantAuditingAndOptimisticVersion() {
        String code = "test-" + UUID.randomUUID();
        ProductDepositEntity product = new ProductDepositEntity();
        product.setCode(code);
        product.setTitle("محصول سپرده آزمایشی");
        product.setStatus(ProductStatus.ACTIVE);
        product.setDepositType(DepositType.DEPOSIT1);
        entityManager.persist(product);

        DepositEntity deposit = new DepositEntity();
        deposit.setDepositNumber(code);
        deposit.setProductCode(code);
        deposit.setDepositStatus(DepositStatus.OPN);
        deposit.setBalance(new BigDecimal("1000"));
        deposit.setCustomerId(100L);
        entityManager.persist(deposit);

        DepositPersonEntity person = new DepositPersonEntity();
        person.setDeposit(deposit);
        person.setCustomerId(100L);
        person.setDepositPersonType(DepositPersonType.values()[0]);
        entityManager.persist(person);
        entityManager.flush();
        entityManager.clear();

        ProductDepositEntity storedProduct = entityManager.find(ProductDepositEntity.class, product.getId());
        assertEquals("محصول سپرده آزمایشی", storedProduct.getTitle());
        assertEquals(fundId, storedProduct.getFundId());
        assertNotNull(storedProduct.getCreatedOn());
        assertEquals("test-operator", storedProduct.getCreatedBy());
        assertEquals("test-operator", storedProduct.getModifiedBy());

        DepositPersonEntity storedPerson = entityManager.find(DepositPersonEntity.class, person.getId());
        assertEquals(fundId, storedPerson.getFundId());
        assertEquals(deposit.getId(), storedPerson.getDeposit().getId());

        DepositEntity storedDeposit = storedPerson.getDeposit();
        assertEquals(fundId, storedDeposit.getFundId());
        assertNotNull(storedDeposit.getCreatedOn());
        assertEquals(0L, storedDeposit.getVersion());
        storedDeposit.setDescription("ویرایش سپرده");
        entityManager.flush();
        entityManager.clear();

        DepositEntity updated = entityManager.find(DepositEntity.class, deposit.getId());
        assertEquals(1L, updated.getVersion());
        assertEquals("ویرایش سپرده", updated.getDescription());
        assertNotNull(updated.getModifiedOn());
        assertEquals("test-operator", updated.getCreatedBy());
        assertEquals("test-operator", updated.getModifiedBy());
    }
}
