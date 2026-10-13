package com.misc.shafagh;
import static org.junit.jupiter.api.Assertions.*;
import com.misc.shafagh.cif.impl.entity.CustomerEntity;
import com.misc.shafagh.cif.impl.entity.enums.*;
import jakarta.persistence.EntityManagerFactory;
import java.util.Date;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties="shafagh.fund-id=57")
class CustomerIdentifierUniquenessTest {
    @Autowired EntityManagerFactory factory;
    CustomerEntity customer(String identifier) {
        var entity=new CustomerEntity();entity.setIdentifier(identifier);entity.setCustomerType(CustomerType.REAL);
        entity.setCustomerStatus(CustomerStatus.ACTIVE);entity.setRegistrationDate(new Date());entity.setCityId(1L);
        entity.setFirstName("علی");entity.setLastName("رضایی");return entity;
    }
    @Test void permitsSameIdentifierAcrossFundsAndDatabaseRejectsDuplicateWithinFund() {
        var sessions=factory.unwrap(SessionFactory.class);String identifier="00"+UUID.randomUUID();
        Long firstId=null;
        for(long fund:new long[]{57,58}) try(var session=sessions.withOptions().tenantIdentifier(fund).openSession()) {
            var tx=session.beginTransaction();var entity=customer(identifier);session.persist(entity);session.flush();
            assertEquals(fund,entity.getFundId());if(firstId==null) firstId=entity.getId();else assertNotEquals(firstId,entity.getId());tx.commit();
        }
        for(long fund:new long[]{57,58}) try(var session=sessions.withOptions().tenantIdentifier(fund).openSession()) {
            assertEquals(1,session.createQuery("from CustomerEntity where identifier=:identifier",CustomerEntity.class)
                .setParameter("identifier",identifier).getResultList().size());
            var tx=session.beginTransaction();
            var error=assertThrows(org.hibernate.exception.ConstraintViolationException.class,()->{session.persist(customer(identifier));session.flush();});
            assertTrue(error.getConstraintName().toUpperCase(java.util.Locale.ROOT).contains("UK_CUSTOMER_FUND_ID_IDENTIFIER"));tx.rollback();
        }
    }
}
