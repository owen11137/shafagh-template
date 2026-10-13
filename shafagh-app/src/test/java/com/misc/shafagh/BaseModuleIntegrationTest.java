package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.base.api.*;
import com.misc.shafagh.base.api.dto.*;
import com.misc.shafagh.base.impl.entity.CityEntity;
import com.misc.shafagh.base.impl.entity.branch.*;
import com.misc.shafagh.base.impl.entity.fund.*;
import com.misc.shafagh.cif.api.CustomerLookup;
import com.misc.shafagh.cif.api.dto.CustomerExistenceStatus;
import com.misc.shafagh.cif.impl.dto.request.*;
import com.misc.shafagh.cif.impl.dto.request.CustomerFilterDTO;
import com.misc.shafagh.cif.impl.entity.enums.*;
import com.misc.shafagh.cif.impl.service.api.CustomerRealService;
import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.contact.ContactType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties="shafagh.fund-id=37")
@Transactional
@org.springframework.context.annotation.Import(BaseModuleIntegrationTest.QueryCaptureConfiguration.class)
class BaseModuleIntegrationTest {
    private static final java.util.Queue<String> capturedQueries = new java.util.concurrent.ConcurrentLinkedQueue<>();
    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods=false)
    static class QueryCaptureConfiguration {
        @org.springframework.context.annotation.Bean
        org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer lookupQueryCapture() {
            return properties -> properties.put("hibernate.session_factory.statement_inspector",
                (org.hibernate.resource.jdbc.spi.StatementInspector) sql -> { capturedQueries.add(sql); return sql; });
        }
    }
    @Autowired private CityService cities;
    @Autowired private FundService funds;
    @Autowired private BranchService branches;
    @Autowired private CustomerRealService customers;
    @Autowired private CustomerLookup customerLookup;
    @PersistenceContext private EntityManager em;

    @Test
    void baseAndCifCommunicateThroughApisAndPersistInTheirOwnSchemas() {
        CityDto province=new CityDto();province.setName("استان تهران");province.setParentId(0L);
        province=cities.save(province);
        CityDto city=new CityDto();city.setName("تهران");city.setParentId(province.getId());
        city=cities.save(city);
        Long cityId=city.getId();
        assertEquals("استان تهران",cities.getById(cityId).getProvince());
        assertTrue(cities.findAll().stream().anyMatch(c -> cityId.equals(c.getId())));
        assertTrue(cities.findAll().stream().noneMatch(c -> Long.valueOf(0L).equals(c.getParentId())));
        CustomerReqDTO customer=new CustomerReqDTO();
        customer.setIdentifier(UUID.randomUUID().toString());customer.setCustomerType(CustomerType.REAL);
        customer.setRegistrationDate(new Date());customer.setCityId(cityId);
        customer.setFirstName("علی");customer.setLastName("رضایی");customer.setFatherName("محمد");
        customer.setGender(Gender.MALE);customer.setMaritalStatus(MaritalStatus.SINGLE);
        CustomerAddressReqDTO address=new CustomerAddressReqDTO();address.setCityId(cityId);address.setAddressType(AddressType.HOME);address.setAddress("نشانی");
        CustomerContactReqDTO contact=new CustomerContactReqDTO();contact.setMobileNumber("09120000000");
        customer.setAddresses(new java.util.ArrayList<>(java.util.List.of(address)));
        customer.setContacts(new java.util.ArrayList<>(java.util.List.of(contact)));
        Long ownerId=customers.save(customer).getId();
        assertTrue(customerLookup.exists(ownerId));
        FundDto fund=new FundDto();fund.setCode("F-"+UUID.randomUUID());fund.setName("صندوق");fund.setOwnerId(ownerId);
        em.createNativeQuery("insert into " + SchemaName.BASE + ".FUND(ID,CODE,NAME,OWNER_ID) values(37,:code,:name,:owner)")
            .setParameter("code",fund.getCode()).setParameter("name",fund.getName()).setParameter("owner",ownerId).executeUpdate();
        fund=funds.getById(37L);
        BranchDto branch=new BranchDto();branch.setCode("B1");branch.setName("شعبه");branch.setFundId(fund.getId());
        branch=branches.save(branch);
        em.flush();em.clear();
        assertEquals(ownerId,funds.getById(fund.getId()).getOwnerId());
        assertEquals(fund.getId(),branches.getById(branch.getId()).getFundId());
        assertEquals("تهران",customers.getById(ownerId).getCity().getName());
        CustomerFilterDTO filter=new CustomerFilterDTO();filter.setCityId(cityId);
        assertTrue(customers.search(filter,PageRequest.of(0,20)).stream().anyMatch(c -> ownerId.equals(c.getId())));
        city.setName("شهر جدید");cities.update(cityId,city);
        fund.setName("صندوق جدید");funds.update(fund.getId(),fund);
        branch.setName("شعبه جدید");branches.update(branch.getId(),branch);
        em.flush();em.clear();
        assertEquals("شهر جدید",customers.getById(ownerId).getCity().getName());
        assertEquals("صندوق جدید",funds.getById(fund.getId()).getName());
        assertEquals("شعبه جدید",branches.getById(branch.getId()).getName());

        BranchEntity branchEntity=em.find(BranchEntity.class,branch.getId());
        FundEntity fundEntity=em.find(FundEntity.class,fund.getId());
        CityEntity cityEntity=em.find(CityEntity.class,cityId);
        BranchAddressEntity ba=new BranchAddressEntity();ba.setBranchEntity(branchEntity);ba.setCity(cityEntity);em.persist(ba);
        FundAddressEntity fa=new FundAddressEntity();fa.setFundEntity(fundEntity);fa.setCity(cityEntity);em.persist(fa);
        BranchContactEntity bc=new BranchContactEntity();bc.setBranch(branchEntity);bc.setContactType(ContactType.PHONE);em.persist(bc);
        FundContactEntity fc=new FundContactEntity();fc.setFundEntity(fundEntity);fc.setBranch(branchEntity);fc.setContactType(ContactType.MOBILE);em.persist(fc);
        em.flush();em.clear();
        assertEquals(fund.getId(),em.find(BranchEntity.class,branch.getId()).getFund().getId());
        assertEquals(cityId,em.find(BranchAddressEntity.class,ba.getId()).getCity().getId());
        assertEquals(cityId,em.find(FundAddressEntity.class,fa.getId()).getCity().getId());
        for(String table:new String[]{"CITY","FUND","BRANCH","BRANCH_ADDRESS","BRANCH_CONTACT","FUND_ADDRESS","FUND_CONTACT"}) {
            Number count=(Number)em.createNativeQuery("select count(*) from "+SchemaName.BASE+"."+table).getSingleResult();
            assertTrue(count.intValue()>0,table);
        }
        Number oldTables=(Number)em.createNativeQuery("select count(*) from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA=:schema and TABLE_NAME in ('FUND','BRANCH','BRANCH_ADDRESS','FUND_ADDRESS','FUND_CONTACT','BRANCH_CONTACT','CITY')")
                .setParameter("schema",SchemaName.CIF).getSingleResult();
        assertEquals(0,oldTables.intValue());
    }

    @Test
    void identifierLookupUsesExactValueAndCurrentFundRegardlessOfStatusAndType() {
        String identifier="00"+UUID.randomUUID();
        var customer=new com.misc.shafagh.cif.impl.entity.CustomerEntity();
        customer.setIdentifier(identifier);customer.setCustomerType(CustomerType.LEGAL);
        customer.setCustomerStatus(CustomerStatus.INACTIVE);customer.setRegistrationDate(new Date());
        customer.setCityId(1L);em.persist(customer);em.flush();em.clear();
        capturedQueries.clear();
        assertEquals(CustomerExistenceStatus.EXIST, customerLookup.existsByIdentifier(identifier));
        String lookupSql = capturedQueries.stream().filter(sql -> sql.toLowerCase(java.util.Locale.ROOT).contains("count("))
            .findFirst().orElseThrow().toLowerCase(java.util.Locale.ROOT);
        assertTrue(lookupSql.contains("identifier=?") || lookupSql.contains("identifier = ?"), lookupSql);
        assertTrue(lookupSql.contains("fund_id=?") || lookupSql.contains("fund_id = ?"), lookupSql);
        assertEquals(CustomerExistenceStatus.NOT_EXIST, customerLookup.existsByIdentifier(identifier.substring(2)));
        assertEquals(CustomerExistenceStatus.NOT_EXIST, customerLookup.existsByIdentifier(" " + identifier));
        assertEquals(CustomerExistenceStatus.NOT_EXIST, customerLookup.existsByIdentifier(UUID.randomUUID().toString()));
        assertEquals(CustomerExistenceStatus.NOT_EXIST, customerLookup.existsByIdentifier(null));
        assertEquals(CustomerExistenceStatus.NOT_EXIST, customerLookup.existsByIdentifier(""));
        assertEquals(CustomerExistenceStatus.NOT_EXIST, customerLookup.existsByIdentifier("   "));
        String otherFund="OTHER-"+UUID.randomUUID();
        em.createNativeQuery("insert into " + SchemaName.CIF + ".CUSTOMER (ID,FUND_ID,IDENTIFIER,CUSTOMER_TYPE,CUSTOMER_STATUS,REGISTRATION_DATE,CITY_ID) values(88888888,38,:identifier,0,1,CURRENT_TIMESTAMP,1)")
            .setParameter("identifier",otherFund).executeUpdate();
        assertEquals(CustomerExistenceStatus.NOT_EXIST, customerLookup.existsByIdentifier(otherFund));
    }

    @Test
    void rejectsUnknownCitiesOwnersFundsAndInvalidCreateRequests() {
        assertThrows(ResponseStatusException.class,()->cities.getById(-1L));
        assertThrows(ResponseStatusException.class,()->cities.save(null));
        FundDto fund=new FundDto();fund.setName("صندوق");fund.setCode("F");fund.setOwnerId(-1L);
        assertThrows(ResponseStatusException.class,()->funds.save(fund));
        BranchDto branch=new BranchDto();branch.setName("شعبه");branch.setCode("B");branch.setFundId(-1L);
        assertThrows(ResponseStatusException.class,()->branches.save(branch));
        assertFalse(customerLookup.exists(-1L));
    }
}
