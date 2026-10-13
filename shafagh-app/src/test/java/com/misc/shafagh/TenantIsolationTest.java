package com.misc.shafagh;
import static org.junit.jupiter.api.Assertions.*;
import com.misc.shafagh.config.persistence.FixedFundTenantIdentifierResolver;
import com.misc.shafagh.dpst.impl.entity.*;
import com.misc.shafagh.common.product.ProductStatus;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties="shafagh.fund-id=57")
class TenantIsolationTest {
    @Autowired EntityManagerFactory factory;
    ProductDepositEntity product() {
        var entity=new ProductDepositEntity();entity.setCode(java.util.UUID.randomUUID().toString());
        entity.setDepositType(DepositType.DEPOSIT1);entity.setStatus(ProductStatus.ACTIVE);return entity;
    }
    @Test void stampsTenantScopesQueriesAndRejectsWritesToAnotherFund() {
        var sessions=factory.unwrap(SessionFactory.class);Long id;
        try(var session=sessions.openSession()) {
            var tx=session.beginTransaction();var product=product();session.persist(product);session.flush();
            assertEquals(57L,product.getFundId());id=product.getId();tx.commit();
        }
        try(var other=sessions.withOptions().tenantIdentifier(58L).openSession()) {
            var tx=other.beginTransaction();
            assertTrue(other.createQuery("from ProductDepositEntity where id = :id",ProductDepositEntity.class).setParameter("id",id).getResultList().isEmpty());
            assertNull(other.find(ProductDepositEntity.class,id));
            var product=product();other.persist(product);other.flush();assertEquals(58L,product.getFundId());tx.commit();
        }
        try(var session=sessions.openSession()) {
            var tx=session.beginTransaction();var product=product();product.setFundId(58L);
            assertThrows(org.hibernate.HibernateException.class,()->{session.persist(product);session.flush();});tx.rollback();
        }
    }
    @Test void fixedResolverRequiresPositiveFund() {
        assertEquals(57L,new FixedFundTenantIdentifierResolver(57).resolveCurrentTenantIdentifier());
        assertTrue(new FixedFundTenantIdentifierResolver(57).validateExistingCurrentSessions());
        assertThrows(IllegalArgumentException.class,()->new FixedFundTenantIdentifierResolver(0));
    }
}
