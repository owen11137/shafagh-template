package com.misc.shafagh;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
/** Verifies actual page boundaries/sorting and the query form generated for Swagger UI. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaginationTest {
    @Autowired ServletWebServerApplicationContext context;
    @Autowired DataSource source;
    RestClient client() { return RestClient.create("http://localhost:"+context.getWebServer().getPort()); }
    @Test void listsArePagedInTheDatabaseAndCityCountsExcludeProvinces() {
        var jdbc=new JdbcTemplate(source);
        jdbc.update("insert into SHFQ_BASE.CITY(ID,NAME,PARENT_ID) values(8000000,'Pagination province',0)");
        for(long id=8000001;id<=8000005;id++) {
            jdbc.update("insert into SHFQ_BASE.CITY(ID,NAME,PARENT_ID) values(?,?,8000000)",id,"City "+id);
            jdbc.update("insert into SHFQ_BASE.FUND(ID,CODE,NAME,OWNER_ID) values(?,?,?,?)",id,"F-"+id,"Fund "+id,id);
            jdbc.update("insert into SHFQ_BASE.BRANCH(ID,CODE,NAME,FUND_ID) values(?,?,?,?)",id,"B-"+id,"Branch "+id,id);
            jdbc.update("insert into SHFQ_DPST.PRODUCT_DEPOSIT(ID,CODE,STATUS,DEPOSIT_TYPE,FUND_ID,OPT_LOCK) values(?,?,0,0,1,0)",id,"DP-"+id);
            jdbc.update("insert into SHFQ_LOAN.PRODUCT_LOAN(ID,CODE,PRODUCT_TYPE,EARLY_SETTLEMENT_ALLOWED,FUND_ID,OPT_LOCK,INSTALLMENT_INTERVAL,MIN_INSTALLMENT_COUNT,MAX_INSTALLMENT_COUNT) values(?,?,0,true,1,0,1,1,12)",id,"LN-"+id);
        }
        for(String path:List.of("/base/city","/base/fund","/base/branch","/dpst/products","/loan/products")) {
            JsonNode first=client().get().uri(path+"?page=0&size=2&sort=id,desc").retrieve().body(JsonNode.class);
            JsonNode second=client().get().uri(path+"?page=1&size=2&sort=id,desc").retrieve().body(JsonNode.class);
            assertEquals(2,first.path("content").size(),path);assertEquals(2,first.path("size").asInt());
            assertEquals(0,first.path("number").asInt());assertEquals(1,second.path("number").asInt());
            assertTrue(first.path("totalElements").asLong()>=5);assertTrue(first.path("totalPages").asInt()>=3);
            assertEquals(8000005L,first.path("content").get(0).path("id").asLong(),path);
            assertEquals(8000004L,first.path("content").get(1).path("id").asLong(),path);
            assertEquals(8000003L,second.path("content").get(0).path("id").asLong(),path);
            JsonNode empty=client().get().uri(path+"?page=999999&size=2").retrieve().body(JsonNode.class);
            assertEquals(0,empty.path("content").size());
            assertEquals(20,client().get().uri(path).retrieve().body(JsonNode.class).path("size").asInt());
            if(path.equals("/base/city")) {
                assertEquals("Pagination province",first.path("content").get(0).path("province").asText());
                assertEquals(jdbc.queryForObject("select count(*) from SHFQ_BASE.CITY where PARENT_ID <> 0 or PARENT_ID is null",Long.class),first.path("totalElements").asLong());
            }
        }
    }
    @Test void swaggerExposesSeparatePaginationQueryParametersAndProductDocumentationInEveryGroup() {
        for(String group:List.of("","/00-all","/01-base","/02-cif","/03-dpst","/04-loan")) {
            JsonNode document=client().get().uri("/v3/api-docs"+group).retrieve().body(JsonNode.class);
            for(String path:List.of("/base/city","/base/fund","/base/branch","/dpst/products","/loan/products","/customer/real/filter","/customer/legal/filter")) {
                if(!document.path("paths").has(path)) continue;
                JsonNode operation=document.path("paths").path(path).path(path.startsWith("/customer")?"post":"get");
                Map<String,JsonNode> params=new HashMap<>();
                for(var parameter:operation.path("parameters")) params.put(parameter.path("name").asText(),parameter);
                assertTrue(params.keySet().containsAll(List.of("page","size","sort")),path+group);assertFalse(params.containsKey("pageable"));
                for(String name:List.of("page","size","sort")) {
                    assertEquals("query",params.get(name).path("in").asText());assertFalse(params.get(name).path("description").asText().isBlank());
                }
                assertEquals(0,params.get("page").path("schema").path("default").asInt());
                assertEquals(20,params.get("size").path("schema").path("default").asInt());
                assertEquals("array",params.get("sort").path("schema").path("type").asText());
                if(path.endsWith("/products")) {
                    assertFalse(operation.path("summary").asText().isBlank());assertFalse(operation.path("description").asText().isBlank());
                    String schema=path.startsWith("/dpst")?"ProductDepositReqDTO":"ProductLoanReqDTO";
                    assertFalse(document.path("components").path("schemas").path(schema).path("description").asText().isBlank());
                }
            }
        }
    }
}
