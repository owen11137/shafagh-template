package com.misc.shafagh;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
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
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.JsonNode;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
    "shafagh.persistence.module-datasources=true", "shafagh.fund-id=43"})
class CustomerLookupHttpTest {
    @Autowired ServletWebServerApplicationContext context;
    @Autowired @Qualifier("cifDataSource") DataSource source;
    @DynamicPropertySource static void databases(DynamicPropertyRegistry registry) {
        for(String module:List.of("base","cif","dpst","loan")) {
            String prefix="shafagh.persistence."+module+".";
            registry.add(prefix+"url",()->"jdbc:h2:mem:lookup_http_"+module+";MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SHFQ_"+module.toUpperCase());
            registry.add(prefix+"username",()->"shfq_"+module);registry.add(prefix+"password",()->"shfq_"+module);
            registry.add(prefix+"driver-class-name",()->"org.h2.Driver");
        }
    }
    RestClient client() { return RestClient.create("http://localhost:"+context.getWebServer().getPort()); }
    void seed(String identifier,long fund,int type,int status) {
        new JdbcTemplate(source).update("insert into SHFQ_CIF.CUSTOMER(ID,FUND_ID,IDENTIFIER,CUSTOMER_TYPE,CUSTOMER_STATUS,REGISTRATION_DATE,CITY_ID) values(SHFQ_CIF.SEQ_CUSTOMER_ID.nextval,?,?,?,?,CURRENT_TIMESTAMP,1)",fund,identifier,type,status);
    }
    Boolean lookup(String identifier) { return client().get().uri("/customer/exists?identifier={identifier}",identifier).retrieve().body(Boolean.class); }
    @Test void checksIndividualsAndCompaniesOnlyInCurrentFundThroughHttp() {
        seed("0012345678",43,0,1);seed("0012345678",44,0,1);
        seed("12345678901",43,1,0);seed("0054321987",44,0,1);
        assertEquals(true,lookup("0012345678"));assertEquals(true,lookup("12345678901"));
        assertEquals(false,lookup("0054321987"));assertEquals(false,lookup("not-registered"));
        assertEquals(false,lookup("12345678"));
        assertEquals(false,client().get().uri("/customer/exists?identifier=0054321987&fundId=44").retrieve().body(Boolean.class));
    }
    @Test void rejectsMissingBlankAndOversizedInputs() {
        for(String value:List.of("","   ","X".repeat(256))) {
            var error=assertThrows(HttpClientErrorException.class,()->lookup(value));assertEquals(400,error.getStatusCode().value());
        }
        var error=assertThrows(HttpClientErrorException.class,()->client().get().uri("/customer/exists").retrieve().toBodilessEntity());
        assertEquals(400,error.getStatusCode().value());
    }
    @Test void documentsLookupInGlobalAndCifSwaggerWithoutClientTenantParameter() {
        for(String group:List.of("","/00-all","/02-cif")) {
            JsonNode operation=client().get().uri("/v3/api-docs"+group).retrieve().body(JsonNode.class)
                .path("paths").path("/customer/exists").path("get");
            assertEquals("بررسی وجود مشتری در صندوق جاری",operation.path("summary").asText());
            assertEquals("boolean",operation.path("responses").path("200").path("content").path("application/json").path("schema").path("type").asText());
            assertEquals(1,operation.path("parameters").size());
            assertEquals("identifier",operation.path("parameters").get(0).path("name").asText());
            assertTrue(operation.path("parameters").get(0).path("required").asBoolean());
        }
    }
}
