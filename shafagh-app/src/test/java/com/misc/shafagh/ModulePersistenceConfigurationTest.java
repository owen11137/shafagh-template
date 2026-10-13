package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.base.api.*;
import com.misc.shafagh.base.api.dto.*;
import com.misc.shafagh.cif.impl.dto.request.*;
import com.misc.shafagh.cif.impl.entity.enums.*;
import com.misc.shafagh.cif.impl.service.api.CustomerRealService;
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
    @Autowired @Qualifier("loanTransactionManager") private PlatformTransactionManager loanTransactions;
    @PersistenceContext(unitName="loan") private EntityManager loan;

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
        assertEquals(5, factories.get("loanEntityManagerFactory").getMetamodel().getEntities().size());
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
        try (var setup=factories.get("baseEntityManagerFactory").createEntityManager()) {
            var tx=setup.getTransaction();tx.begin();
            setup.createNativeQuery("update SHFQ_BASE.FUND set ID=43 where ID=:id").setParameter("id",fund.getId()).executeUpdate();tx.commit();
        }
        fund=funds.getById(43L);
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
    @Test
    void loanModelsPersistInLoanUnitWithAuditingVersionAndTenantIsolation() {
        var transactions = new TransactionTemplate(loanTransactions);
        Long[] ids = transactions.execute(status -> {
            var product = new com.misc.shafagh.loan.impl.entity.ProductLoanEntity();
            product.setCode("L-" + UUID.randomUUID().toString().substring(0, 12));
            product.setTitle("محصول تسهیلات قرض الحسنه");
            product.setStatus(ProductStatus.ACTIVE);
            product.setProductType(com.misc.shafagh.loan.impl.entity.LoanType.ADI);
            product.setEarlySettlementAllowed(true);
            product.setMinAmount(new java.math.BigDecimal("1000.00"));
            product.setMaxAmount(new java.math.BigDecimal("100000.00"));
            product.setMinInstallmentCount(1); product.setMaxInstallmentCount(24);
            loan.persist(product);
            var entity = new com.misc.shafagh.loan.impl.entity.LoanEntity();
            entity.setLoanNumber(UUID.randomUUID().toString()); entity.setProductCode(product.getCode());
            entity.setAmount(new java.math.BigDecimal("5000.25")); entity.setInstallmentCount(12);
            entity.setFeeRate(new java.math.BigDecimal("4.00"));
            entity.setLoanStatus(com.misc.shafagh.loan.impl.entity.LoanStatus.OPN);
            loan.persist(entity);
            var person = new com.misc.shafagh.loan.impl.entity.LoanPersonEntity();
            person.setLoan(entity); person.setCustomerId(100L);
            person.setCollateralType(com.misc.shafagh.loan.impl.entity.CollateralType.CHEQUE);
            person.setCollateralNumber("CHEQUE-123");
            loan.persist(person); loan.flush();
            return new Long[]{product.getId(), entity.getId(), person.getId()};
        });
        transactions.executeWithoutResult(status -> {
            var product = loan.find(com.misc.shafagh.loan.impl.entity.ProductLoanEntity.class, ids[0]);
            var entity = loan.find(com.misc.shafagh.loan.impl.entity.LoanEntity.class, ids[1]);
            assertEquals("محصول تسهیلات قرض الحسنه", product.getTitle());
            assertEquals(43L, product.getFundId()); assertNotNull(product.getCreatedOn());
            assertEquals(43L, entity.getFundId()); assertNotNull(entity.getCreatedOn());
            var person = loan.find(com.misc.shafagh.loan.impl.entity.LoanPersonEntity.class, ids[2]);
            assertEquals(43L, person.getFundId()); assertNotNull(person.getCreatedOn());
            assertEquals(100L, person.getCustomerId()); assertEquals(entity.getId(), person.getLoan().getId());
            assertEquals(com.misc.shafagh.loan.impl.entity.LoanPersonType.OWNER, person.getLoanPersonType());
            assertEquals(com.misc.shafagh.loan.impl.entity.CollateralType.CHEQUE, person.getCollateralType());
            assertEquals("CHEQUE-123", person.getCollateralNumber());
            assertEquals(0L, entity.getVersion());
            assertEquals(0, new java.math.BigDecimal("5000.25").compareTo(entity.getAmount()));
            entity.setLoanStatus(com.misc.shafagh.loan.impl.entity.LoanStatus.ACT);
            loan.flush();
        });
        transactions.executeWithoutResult(status -> {
            var entity = loan.find(com.misc.shafagh.loan.impl.entity.LoanEntity.class, ids[1]);
            assertEquals(1L, entity.getVersion()); assertNotNull(entity.getModifiedOn());
            assertEquals(com.misc.shafagh.loan.impl.entity.LoanStatus.ACT, entity.getLoanStatus());
        });
        var factory = factories.get("loanEntityManagerFactory").unwrap(org.hibernate.SessionFactory.class);
        try (var anotherTenant = factory.withOptions().tenantIdentifier(99L).openSession()) {
            assertTrue(anotherTenant.createQuery("from Loan", com.misc.shafagh.loan.impl.entity.LoanEntity.class)
                    .getResultList().isEmpty());
        }
    }
    @Test
    void installmentsAndPaymentsPersistInLoanSchemaWithTenantAndAuditFields() {
        var transactions = new TransactionTemplate(loanTransactions);
        Long[] ids = transactions.execute(status -> {
            var entity = new com.misc.shafagh.loan.impl.entity.LoanEntity();
            entity.setLoanNumber(UUID.randomUUID().toString()); entity.setProductCode("INSTALLMENT-TEST");
            entity.setAmount(new java.math.BigDecimal("1200.00")); entity.setInstallmentCount(12);
            loan.persist(entity);
            var installment = new com.misc.shafagh.loan.impl.entity.installment.InstallmentEntity();
            installment.setLoan(entity); installment.setInstallmentNumber(1);
            installment.setInstallmentType(com.misc.shafagh.loan.impl.entity.installment.InstallmentType.MAIN);
            installment.setInstallmentStatus(com.misc.shafagh.loan.impl.entity.installment.InstallmentStatus.PND);
            installment.setInstallmentAmount(new java.math.BigDecimal("100.25"));
            installment.setInstallmentDate(java.time.LocalDateTime.of(2026, 11, 12, 10, 30));
            loan.persist(installment);
            var payment = new com.misc.shafagh.loan.impl.entity.installment.InstallmentDetailEntity();
            payment.setLoan(entity); payment.setInstallmentNumber(1);
            payment.setPaidAmount(new java.math.BigDecimal("50.25"));
            payment.setFineAmount(new java.math.BigDecimal("1.25"));
            payment.setPaidDate(java.time.LocalDateTime.of(2026, 11, 13, 11, 30));
            payment.setTransactionNO(123456L);
            loan.persist(payment); loan.flush();
            return new Long[]{entity.getId(), installment.getId(), payment.getId()};
        });
        transactions.executeWithoutResult(status -> {
            var installment = loan.find(com.misc.shafagh.loan.impl.entity.installment.InstallmentEntity.class, ids[1]);
            var payment = loan.find(com.misc.shafagh.loan.impl.entity.installment.InstallmentDetailEntity.class, ids[2]);
            assertEquals(ids[0], installment.getLoan().getId()); assertEquals(ids[0], payment.getLoan().getId());
            assertEquals(43L, installment.getFundId()); assertEquals(43L, payment.getFundId());
            assertNotNull(installment.getCreatedOn()); assertNotNull(payment.getCreatedOn());
            assertEquals(0L, installment.getVersion());
            assertEquals(com.misc.shafagh.loan.impl.entity.installment.InstallmentType.MAIN, installment.getInstallmentType());
            assertEquals(com.misc.shafagh.loan.impl.entity.installment.InstallmentStatus.PND, installment.getInstallmentStatus());
            assertEquals(0, new java.math.BigDecimal("100.25").compareTo(installment.getInstallmentAmount()));
            assertEquals(0, new java.math.BigDecimal("50.25").compareTo(payment.getPaidAmount()));
            assertEquals(0, new java.math.BigDecimal("1.25").compareTo(payment.getFineAmount()));
            assertEquals(123456L, payment.getTransactionNO());
            installment.setInstallmentStatus(com.misc.shafagh.loan.impl.entity.installment.InstallmentStatus.PSP);
            loan.flush();
        });
        transactions.executeWithoutResult(status -> {
            var installment = loan.find(com.misc.shafagh.loan.impl.entity.installment.InstallmentEntity.class, ids[1]);
            assertEquals(1L, installment.getVersion()); assertNotNull(installment.getModifiedOn());
            assertEquals(com.misc.shafagh.loan.impl.entity.installment.InstallmentStatus.PSP, installment.getInstallmentStatus());
        });
    }
    @Test
    void databaseConstraintsUseReadableNamesInEveryModule() {
        for (String module : List.of("base", "cif", "dpst", "loan")) {
            String schema = "SHFQ_" + module.toUpperCase();
            var jdbc = new JdbcTemplate(sources.get(module + "DataSource"));
            var constraints = jdbc.queryForList("select TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where CONSTRAINT_SCHEMA=?", schema);
            assertFalse(constraints.isEmpty(), module);
            for (var row : constraints) {
                String type = row.get("CONSTRAINT_TYPE").toString();
                String name = row.get("CONSTRAINT_NAME").toString();
                String table = row.get("TABLE_NAME").toString();
                switch (type) {
                    case "PRIMARY KEY" -> assertFalse(name.isBlank());
                    case "FOREIGN KEY" -> assertTrue(name.startsWith("FK_" + table + "_"), name);
                    case "UNIQUE" -> assertTrue(name.startsWith("UK_" + table + "_"), name);
                    case "CHECK" -> assertFalse(name.isBlank());
                    default -> fail("Unknown constraint type " + type);
                }
            }
            for (var entity : factories.get(module + "EntityManagerFactory").getMetamodel().getEntities()) {
                var sequence = entity.getJavaType().getAnnotation(jakarta.persistence.SequenceGenerator.class);
                String table = entity.getJavaType().getAnnotation(Table.class).name();
                assertEquals("SEQ_" + table + "_ID", sequence.sequenceName());
                assertTrue(sequence.sequenceName().length() <= 50);
            }
        }
    }
}
