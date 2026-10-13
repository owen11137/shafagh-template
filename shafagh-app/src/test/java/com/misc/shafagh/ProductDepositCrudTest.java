package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.dpst.impl.dto.response.ProductDepositResponseDto;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "shafagh.persistence.module-datasources=true", "shafagh.fund-id=43"
})
class ProductDepositCrudTest {
    @Autowired private ServletWebServerApplicationContext context;
    @Autowired @Qualifier("dpstDataSource") private DataSource source;

    @DynamicPropertySource
    static void databases(DynamicPropertyRegistry registry) {
        for (String module : List.of("base", "cif", "dpst", "loan")) {
            String prefix = "shafagh.persistence." + module + ".";
            registry.add(prefix + "url", () -> "jdbc:h2:mem:deposit_product_crud_" + module
                    + ";MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SHFQ_" + module.toUpperCase());
            registry.add(prefix + "username", () -> "shfq_" + module);
            registry.add(prefix + "password", () -> "shfq_" + module);
            registry.add(prefix + "driver-class-name", () -> "org.h2.Driver");
        }
    }

    @Test void completeCrudPreservesTenantAuditAndPersistsAllFields() {
        var request=request();var created=client().post().uri("/dpst/products").body(request).retrieve().toEntity(ProductDepositResponseDto.class);
        assertEquals(201,created.getStatusCode().value());var product=created.getBody();assertNotNull(product);
        assertEquals(43L,product.getFundId());assertNotNull(product.getCreatedOn());assertEquals(0L,product.getVersion());
        var read=client().get().uri("/dpst/products/{id}",product.getId()).retrieve().body(ProductDepositResponseDto.class);
        assertEquals(request.get("description"),read.getDescription());assertEquals(request.get("code"),read.getCode());
        request.put("title","محصول ویرایش شده");request.put("status","INACTIVE");request.put("depositType","DEPOSIT2");
        var updated=client().put().uri("/dpst/products/{id}",product.getId()).body(request).retrieve().body(ProductDepositResponseDto.class);
        assertEquals("محصول ویرایش شده",updated.getTitle());assertEquals("INACTIVE",updated.getStatus().name());
        assertEquals("DEPOSIT2",updated.getDepositType().name());assertEquals(product.getCreatedOn(),updated.getCreatedOn());
        assertEquals(1L,updated.getVersion());assertNotNull(updated.getModifiedOn());
        var page=client().get().uri("/dpst/products?size=100&sort=id,desc").retrieve().body(tools.jackson.databind.JsonNode.class);
        boolean found=false;for(var row:page.path("content")) if(row.path("id").asLong()==product.getId()) found=true;
        assertTrue(found);
        assertEquals(204,client().delete().uri("/dpst/products/{id}",product.getId()).retrieve().toBodilessEntity().getStatusCode().value());
        status(404,()->client().get().uri("/dpst/products/{id}",product.getId()).retrieve().toBodilessEntity());
    }
    @Test void validatesRequiredFieldsLengthsDatesAndEnumValues() {
        for(String field:List.of("code","status","depositType")) {
            var invalid=request();invalid.remove(field);status(400,()->create(invalid));
        }
        for(String field:List.of("title","code","description")) {
            var invalid=request();invalid.put(field,"x".repeat(256));status(400,()->create(invalid));
        }
        var blank=request();blank.put("code","  ");status(400,()->create(blank));
        var invalidEnum=request();invalidEnum.put("depositType","UNKNOWN");status(400,()->create(invalidEnum));
        var dates=request();dates.put("fromDate","2027-12-01T00:00:00");dates.put("toDate","2027-11-01T00:00:00");status(400,()->create(dates));
        status(400,()->client().get().uri("/dpst/products/0").retrieve().toBodilessEntity());
    }
    @Test void handlesDuplicatesMissingRecordsAndForeignFund() {
        var first=request();var product=create(first);status(409,()->create(first));
        var second=create(request());status(409,()->client().put().uri("/dpst/products/{id}",second.getId()).body(first).retrieve().toBodilessEntity());
        var jdbc=new JdbcTemplate(source);String foreign="FOREIGN-"+UUID.randomUUID();
        jdbc.update("insert into SHFQ_DPST.PRODUCT_DEPOSIT(ID,CODE,STATUS,DEPOSIT_TYPE,FUND_ID,OPT_LOCK) values(900000,?,0,0,99,0)",foreign);
        status(404,()->client().get().uri("/dpst/products/900000").retrieve().toBodilessEntity());
        status(404,()->client().put().uri("/dpst/products/900000").body(request()).retrieve().toBodilessEntity());
        status(404,()->client().delete().uri("/dpst/products/900000").retrieve().toBodilessEntity());
        var duplicate=request();duplicate.put("code",foreign);status(409,()->create(duplicate));
        var page=client().get().uri("/dpst/products?size=100").retrieve().body(tools.jackson.databind.JsonNode.class);
        for(var row:page.path("content")) assertEquals(43L,row.path("fundId").asLong());
        status(404,()->client().put().uri("/dpst/products/999999").body(request()).retrieve().toBodilessEntity());
        status(404,()->client().delete().uri("/dpst/products/999999").retrieve().toBodilessEntity());
        assertEquals(product.getCode(),client().get().uri("/dpst/products/{id}",product.getId()).retrieve().body(ProductDepositResponseDto.class).getCode());
    }
    @Test void protectsProductCodeAndDeletionWhenAccountsExist() {
        var request=request();var product=create(request);var jdbc=new JdbcTemplate(source);
        jdbc.update("insert into SHFQ_DPST.DEPOSIT(ID,FUND_ID,DEPOSIT_NUMBER,PRODUCT_CODE,CUSTOMER_ID) values(SHFQ_DPST.SEQ_DEPOSIT_ID.nextval,43,'existing-account',?,1)",product.getCode());
        status(409,()->client().delete().uri("/dpst/products/{id}",product.getId()).retrieve().toBodilessEntity());
        request.put("code","NEW-"+UUID.randomUUID());
        status(409,()->client().put().uri("/dpst/products/{id}",product.getId()).body(request).retrieve().toBodilessEntity());
        request.put("code",product.getCode());request.put("status","INACTIVE");
        var updated=client().put().uri("/dpst/products/{id}",product.getId()).body(request).retrieve().body(ProductDepositResponseDto.class);
        assertEquals("INACTIVE",updated.getStatus().name());
    }
    RestClient client() { return RestClient.create("http://localhost:"+context.getWebServer().getPort()); }
    ProductDepositResponseDto create(Map<String,Object> request) { return client().post().uri("/dpst/products").body(request).retrieve().body(ProductDepositResponseDto.class); }
    Map<String,Object> request() {
        var request=new HashMap<String,Object>();request.put("code","P-"+UUID.randomUUID());
        request.put("title","محصول سپرده");request.put("description","توضیحات محصول");
        request.put("depositType","DEPOSIT1");request.put("status","ACTIVE");return request;
    }
    void status(int expected,Runnable action) {
        var exception=assertThrows(HttpClientErrorException.class,action::run);assertEquals(expected,exception.getStatusCode().value());
    }
}
