package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.cif.impl.dto.request.*;
import com.misc.shafagh.cif.impl.dto.response.CustomerDto;
import com.misc.shafagh.base.impl.entity.CityEntity;
import com.misc.shafagh.cif.impl.entity.CustomerEntity;
import com.misc.shafagh.cif.impl.entity.enums.*;
import com.misc.shafagh.cif.impl.exception.ValidationException;
import com.misc.shafagh.cif.impl.service.api.CustomerRealService;
import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "shafagh.fund-id=37")
@Transactional
class CifCustomerServiceTest {
    @Autowired private CustomerRealService service;
    @PersistenceContext private EntityManager entityManager;

    @Test
    void savesAndUpdatesCustomerGraphWithConfiguredTenantAndSchemas() {
        CustomerReqDTO request = request();
        CustomerDto saved = service.save(request);
        entityManager.flush();
        entityManager.clear();
        CustomerEntity entity = entityManager.find(CustomerEntity.class, saved.getId());
        assertEquals(37L, entity.getFundId());
        assertNotNull(entity.getCreatedOn());
        assertEquals("09120000000", entity.getContacts().getFirst().getMobileNumber());
        assertEquals(37L, entity.getContacts().getFirst().getFundId());
        assertEquals(37L, entity.getAddresses().getFirst().getFundId());
        Number stored = (Number) entityManager.createNativeQuery(
                "select count(*) from " + SchemaName.CIF + ".CUSTOMER where ID = :id")
                .setParameter("id", saved.getId()).getSingleResult();
        assertEquals(1, stored.intValue());

        request.setId(saved.getId());
        request.getAddresses().getFirst().setId(saved.getAddresses().getFirst().getId());
        request.getAddresses().getFirst().setAddress("نشانی جدید");
        request.getContacts().getFirst().setId(saved.getContacts().getFirst().getId());
        request.getContacts().getFirst().setMobileNumber("09120000001");
        service.update(request);
        entityManager.flush();
        entityManager.clear();
        CustomerDto updated = service.getById(saved.getId());
        assertEquals("نشانی جدید", updated.getAddresses().getFirst().getAddress());
        assertEquals("09120000001", updated.getContacts().getFirst().getMobileNumber());
        assertEquals(saved.getCustomerCode(), updated.getCustomerCode());
    }

    @Test
    void rejectsDuplicateIdentifierAndInvalidRequests() {
        CustomerReqDTO request = request();
        service.save(request);
        entityManager.flush();
        assertThrows(ValidationException.class, () -> service.save(request));
        assertThrows(ValidationException.class, () -> service.save(null));
        CustomerReqDTO missingId = new CustomerReqDTO();
        assertThrows(ValidationException.class, () -> service.update(missingId));
        CustomerReqDTO suppliedId = new CustomerReqDTO();
        suppliedId.setId(1L);
        assertThrows(ValidationException.class, () -> service.save(suppliedId));
        CustomerReqDTO invalid = request();
        invalid.setContacts(new ArrayList<>(java.util.Arrays.asList((CustomerContactReqDTO) null)));
        assertThrows(ValidationException.class, () -> service.save(invalid));
    }

    @Test
    void rejectsChangingIdentifierToAnotherCustomer() {
        var firstRequest=request();var first=service.save(firstRequest);
        var secondRequest=request();var second=service.save(secondRequest);
        secondRequest.setId(second.getId());secondRequest.setIdentifier(firstRequest.getIdentifier());
        assertThrows(com.misc.shafagh.cif.impl.exception.DuplicateCustomerIdentifierException.class,()->service.update(secondRequest));
        // An exception marks the surrounding transaction rollback-only, so verify unchanged value directly.
        assertEquals(firstRequest.getIdentifier(),entityManager.find(CustomerEntity.class,first.getId()).getIdentifier());
    }

    private CustomerReqDTO request() {
        CityEntity city = new CityEntity();
        city.setName("تهران");
        city.setParentId(0L);
        entityManager.persist(city);
        entityManager.flush();
        CustomerReqDTO request = new CustomerReqDTO();
        request.setIdentifier(UUID.randomUUID().toString());
        request.setCustomerType(CustomerType.REAL);
        request.setRegistrationDate(new Date());
        request.setCityId(city.getId());
        request.setFirstName("علی");
        request.setLastName("رضایی");
        request.setFatherName("محمد");
        request.setGender(Gender.MALE);
        request.setMaritalStatus(MaritalStatus.SINGLE);
        CustomerAddressReqDTO address = new CustomerAddressReqDTO();
        address.setCityId(city.getId());
        address.setAddressType(AddressType.HOME);
        address.setAddress("نشانی قدیم");
        request.setAddresses(new ArrayList<>(List.of(address)));
        CustomerContactReqDTO contact = new CustomerContactReqDTO();
        contact.setMobileNumber("09120000000");
        request.setContacts(new ArrayList<>(List.of(contact)));
        return request;
    }
}
