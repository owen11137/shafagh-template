package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.base.api.*;
import com.misc.shafagh.base.api.dto.*;
import com.misc.shafagh.cif.impl.dto.request.*;
import com.misc.shafagh.cif.impl.entity.enums.*;
import com.misc.shafagh.cif.impl.service.port.CustomerRealService;
import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.dpst.impl.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties={"shafagh.persistence.module-datasources=true", "shafagh.fund-id=43"})
class ModulePersistenceConfigurationTest {
    @Autowired private Map<String, EntityManagerFactory> factories;
    @Autowired private Map<String, DataSource> sources;
    @Autowired private CityService cities;
    @Autowired private FundService funds;
    @Autowired private BranchService branches;
    @Autowired private CustomerRealService customers;
    @Autowired @Qualifier("dpstTransactionManager") private PlatformTransactionManager dpstTransactions;
    @PersistenceContext(unitName="dpst") private EntityManager dpst;

    @DynamicPropertySource
    static void databases(DynamicPropertyRegistry registry) {
        for(String module:List.of("base","cif","dpst","loan")) {
            String prefix="shafagh.persistence."+module+".";
            registry.add(prefix+"url",()->"jdbc:h2:mem:module_"+module+";MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SHFQ_"+module.toUpperCase());
            registry.add(prefix+"username",()->"shfq_"+module);
            registry.add(prefix+"password",()->"shfq_"+module);
            registry.add(prefix+"driver-class-name",()->"org.h2.Driver");
        }
    }

    @Test
    void everyModuleUsesItsOwnAccountAndCreatesAllMappedTablesAndSequences() throws Exception {
        for(String module:List.of("base","cif","dpst","loan")) {
            EntityManagerFactory factory=factories.get(module+"EntityManagerFactory");
            assertNotNull(factory,module);
            String schema="SHFQ_"+module.toUpperCase();
            var jdbc=new JdbcTemplate(sources.get(module+"DataSource"));
            try(var connection=sources.get(module+"DataSource").getConnection()) {
                assertEquals(schema,connection.getMetaData().getUserName());
            }
            for(var entity:factory.getMetamodel().getEntities()) {
                Class<?> type=entity.getJavaType();
                assertTrue(type.getPackageName().startsWith("com.misc.shafagh."+module+".impl.entity"));
                Table table=type.getAnnotation(Table.class);
                assertEquals(schema,table.schema(),type.getName());
                String name=table.name().isBlank()?entity.getName():table.name();
                assertNotNull(jdbc.queryForObject("select count(*) from "+schema+"."+name,Long.class));
                var sequence=type.getAnnotation(jakarta.persistence.SequenceGenerator.class);
                if(sequence!=null) {
                    assertEquals(schema,sequence.schema());
                    assertEquals(1,jdbc.queryForObject("select count(*) from INFORMATION_SCHEMA.SEQUENCES where SEQUENCE_SCHEMA=? and SEQUENCE_NAME=?",Integer.class,schema,sequence.sequenceName()));
                }
            }
            assertEquals(0,jdbc.queryForObject("select count(*) from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA like 'SHFQ_%' and TABLE_SCHEMA<>?",Integer.class,schema));
        }
        assertFalse(factories.get("baseEntityManagerFactory").getMetamodel().getEntities().isEmpty());
        assertFalse(factories.get("cifEntityManagerFactory").getMetamodel().getEntities().isEmpty());
        assertFalse(factories.get("dpstEntityManagerFactory").getMetamodel().getEntities().isEmpty());
    }

    @Test
    void independentTransactionsSupportCrossModuleLookupsAndDepositAuditing() {
        CityDto city=new CityDto();city.setName("تهران");city.setParentId(0L);city=cities.save(city);
        CustomerReqDTO request=new CustomerReqDTO();request.setIdentifier(UUID.randomUUID().toString());
        request.setCustomerType(CustomerType.REAL);request.setRegistrationDate(new Date());request.setCityId(city.getId());
        request.setFirstName("علی");request.setLastName("رضایی");request.setFatherName("محمد");
        request.setGender(Gender.MALE);request.setMaritalStatus(MaritalStatus.SINGLE);
        CustomerAddressReqDTO address=new CustomerAddressReqDTO();address.setCityId(city.getId());address.setAddress("نشانی");address.setAddressType(AddressType.HOME);
        CustomerContactReqDTO contact=new CustomerContactReqDTO();contact.setMobileNumber("09120000000");
        request.setAddresses(List.of(address));request.setContacts(List.of(contact));
        var customer=customers.save(request);
        assertEquals(city.getId(),customers.getById(customer.getId()).getCity().getId());
        FundDto fund=new FundDto();fund.setOwnerId(customer.getId());fund.setCode("F-"+UUID.randomUUID());fund.setName("صندوق");fund=funds.save(fund);
        BranchDto branch=new BranchDto();branch.setFundId(fund.getId());branch.setCode("B");branch.setName("شعبه");branch=branches.save(branch);
        assertEquals(fund.getId(),branches.getById(branch.getId()).getFundId());
        assertEquals(customer.getId(),funds.getById(fund.getId()).getOwnerId());
        Long productId=new TransactionTemplate(dpstTransactions).execute(status -> {
            ProductDepositEntity product=new ProductDepositEntity();product.setCode(UUID.randomUUID().toString());
            product.setTitle("محصول");product.setStatus(ProductStatus.ACTIVE);product.setDepositType(DepositType.DEPOSIT1);
            dpst.persist(product);dpst.flush();return product.getId();
        });
        new TransactionTemplate(dpstTransactions).executeWithoutResult(status -> {
            ProductDepositEntity stored=dpst.find(ProductDepositEntity.class,productId);
            assertEquals(43L,stored.getFundId());assertNotNull(stored.getCreatedOn());
        });
    }
}
