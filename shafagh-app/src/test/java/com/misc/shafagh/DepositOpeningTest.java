package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;
import com.misc.shafagh.dpst.impl.dto.response.OpenDepositResponse;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
    "shafagh.persistence.module-datasources=true", "shafagh.fund-id=43"})
class DepositOpeningTest {
    @Autowired ServletWebServerApplicationContext context;
    @Autowired @Qualifier("dpstDataSource") DataSource dpst;
    @Autowired @Qualifier("cifDataSource") DataSource cif;
    @DynamicPropertySource static void databases(DynamicPropertyRegistry registry) {
        for (String module : List.of("base","cif","dpst","loan")) {
            String prefix="shafagh.persistence."+module+".";
            registry.add(prefix+"url",()->"jdbc:h2:mem:opening_"+module+";MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SHFQ_"+module.toUpperCase());
            registry.add(prefix+"username",()->"shfq_"+module);
            registry.add(prefix+"password",()->"shfq_"+module);
            registry.add(prefix+"driver-class-name",()->"org.h2.Driver");
        }
    }
    RestClient client() { return RestClient.create("http://localhost:"+context.getWebServer().getPort()); }
    String seed(int status, Long fund, String national) {
        var c=new JdbcTemplate(cif);
        Long id=c.queryForObject("select SHFQ_CIF.SEQ_CUSTOMER_ID.nextval from dual",Long.class);
        c.update("insert into SHFQ_CIF.CUSTOMER(ID,FUND_ID,IDENTIFIER,CUSTOMER_TYPE,CUSTOMER_STATUS,REGISTRATION_DATE,CITY_ID) values(?,?,?,0,?,CURRENT_TIMESTAMP,1)",id,fund,national,1-status);
        return product(0,43L);
    }
    String product(int status,Long fund) {
        String code=UUID.randomUUID().toString();var d=new JdbcTemplate(dpst);
        d.update("insert into SHFQ_DPST.PRODUCT_DEPOSIT(ID,FUND_ID,CODE,STATUS,DEPOSIT_TYPE) values(SHFQ_DPST.SEQ_PRODUCT_DEPOSIT_ID.nextval,?,?,?,0)",fund,code,status);
        return code;
    }
    OpenDepositResponse open(String national,String code) {
        var response=client().post().uri("/dpst/deposits").body(Map.of("nationalCode",national,"productCode",code)).retrieve().toEntity(OpenDepositResponse.class);
        assertEquals(201,response.getStatusCode().value());return response.getBody();
    }
    void reject(int status,String national,String code) {
        var error=assertThrows(RestClientResponseException.class,()->open(national,code));
        assertEquals(status,error.getStatusCode().value());
    }
    @Test void savesAccountOwnerAndUniqueNumbersUnderConcurrentRequests() throws Exception {
        String code=seed(0,43L,"0013549871");
        // Independently generated valid national number; leading zero must remain intact.
        try(var executor=Executors.newFixedThreadPool(4)) {
            List<Callable<OpenDepositResponse>> jobs=new ArrayList<>();
            for(int i=0;i<8;i++) jobs.add(()->open("0013549871",code));
            Set<String> numbers=new HashSet<>();
            var d=new JdbcTemplate(dpst);
            for(var future:executor.invokeAll(jobs)) {
                var account=future.get(); assertTrue(numbers.add(account.depositNumber()));
                assertTrue(account.depositNumber().matches("[0-9]{20}"));
                assertEquals("OPN",account.status().name());assertEquals(0,account.balance().signum());
                assertEquals(account.depositNumber(),d.queryForObject("select DEPOSIT_NUMBER from SHFQ_DPST.DEPOSIT where ID=?",String.class,account.id()));
                assertEquals(1,d.queryForObject("select count(*) from SHFQ_DPST.DEPOSIT_PERSON where DEPOSIT_ID=? and CUSTOMER_ID=? and FUND_ID=43 and DEPOSIT_PERSON_TYPE=0",Integer.class,account.id(),account.customerId()));
                assertNotNull(d.queryForObject("select CREATED_ON from SHFQ_DPST.DEPOSIT where ID=?",java.sql.Timestamp.class,account.id()));
            }
        }
    }
    @Test void rejectsInvalidMissingInactiveExpiredAndOtherFundInputsWithoutWrites() {
        String code=seed(0,43L,"0023549874");
        var d=new JdbcTemplate(dpst);long before=d.queryForObject("select count(*) from SHFQ_DPST.DEPOSIT",Long.class);
        reject(400,"123",code);reject(400,"1111111111",code);reject(400,"0023549870",code);
        reject(404,"0033549877",code);reject(404,"0023549874","missing");
        reject(409,"0023549874",product(1,43L));reject(404,"0023549874",product(0,44L));
        String future=product(0,43L);d.update("update SHFQ_DPST.PRODUCT_DEPOSIT set FROM_DATE=TIMESTAMP '2099-01-01 00:00:00' where CODE=?",future);reject(409,"0023549874",future);
        String expired=product(0,43L);d.update("update SHFQ_DPST.PRODUCT_DEPOSIT set TO_DATE=TIMESTAMP '2000-01-01 00:00:00' where CODE=?",expired);reject(409,"0023549874",expired);
        reject(404,"0043549871",seed(1,43L,"0043549871"));
        reject(404,"0053549872",seed(0,44L,"0053549872"));
        assertEquals(before,d.queryForObject("select count(*) from SHFQ_DPST.DEPOSIT",Long.class));
    }
    @Test void rollsBackDepositWhenOwnerInsertFails() {
        String code=seed(0,43L,"0063549875");var d=new JdbcTemplate(dpst);
        long before=d.queryForObject("select count(*) from SHFQ_DPST.DEPOSIT",Long.class);
        long owners=d.queryForObject("select count(*) from SHFQ_DPST.DEPOSIT_PERSON",Long.class);
        Long rejectedCustomer=new JdbcTemplate(cif).queryForObject("select ID from SHFQ_CIF.CUSTOMER where IDENTIFIER=?",Long.class,"0063549875");
        d.execute("alter table SHFQ_DPST.DEPOSIT_PERSON add constraint TEST_REJECT_OWNER check (CUSTOMER_ID <> " + rejectedCustomer + ")");
        try { reject(500,"0063549875",code); }
        finally { d.execute("alter table SHFQ_DPST.DEPOSIT_PERSON drop constraint TEST_REJECT_OWNER"); }
        assertEquals(before,d.queryForObject("select count(*) from SHFQ_DPST.DEPOSIT",Long.class));
        assertEquals(owners,d.queryForObject("select count(*) from SHFQ_DPST.DEPOSIT_PERSON",Long.class));
    }
}
