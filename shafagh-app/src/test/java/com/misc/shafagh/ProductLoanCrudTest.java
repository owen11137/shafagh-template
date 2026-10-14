package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.loan.impl.dto.response.ProductLoanResponseDto;
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
class ProductLoanCrudTest {
    @Autowired private ServletWebServerApplicationContext context;
    @Autowired @Qualifier("loanDataSource") private DataSource source;

    @DynamicPropertySource
    static void databases(DynamicPropertyRegistry registry) {
        for (String module : List.of("base", "cif", "dpst", "loan")) {
            String prefix = "shafagh.persistence." + module + ".";
            registry.add(prefix + "url", () -> "jdbc:h2:mem:product_crud_" + module
                    + ";MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SHFQ_" + module.toUpperCase());
            registry.add(prefix + "username", () -> "shfq_" + module);
            registry.add(prefix + "password", () -> "shfq_" + module);
            registry.add(prefix + "driver-class-name", () -> "org.h2.Driver");
        }
    }

    @Test
    void createsReadsListsUpdatesAndDeletesThroughHttpWithLoanTransactionManager() {
        var client = client();
        var request = request();
        var created = client.post().uri("/loan/products").body(request).retrieve().toEntity(ProductLoanResponseDto.class);
        assertEquals(201, created.getStatusCode().value());
        var product = created.getBody();
        assertNotNull(product); assertNotNull(product.getId()); assertEquals(43L, product.getFundId());
        assertNotNull(product.getCreatedOn()); assertEquals(0L, product.getVersion());
        Long id = product.getId();
        var read = client.get().uri("/loan/products/{id}", id).retrieve().body(ProductLoanResponseDto.class);
        assertEquals(request.get("code"), read.getCode());
        assertEquals(0, new BigDecimal("100.25").compareTo(read.getMinAmount()));
        var page = client.get().uri("/loan/products?page=0&size=20&sort=id,desc").retrieve()
                .body(tools.jackson.databind.JsonNode.class);
        assertTrue(page.path("content").isArray());
        boolean found = false;
        for (var entry : page.path("content")) if (entry.path("id").asLong() == id) found = true;
        assertTrue(found);
        request.put("title", "محصول ویرایش شده"); request.put("maxAmount", new BigDecimal("20000.00"));
        request.put("earlySettlementAllowed", false);
        var updated = client.put().uri("/loan/products/{id}", id).body(request).retrieve().body(ProductLoanResponseDto.class);
        assertEquals(id, updated.getId()); assertEquals(43L, updated.getFundId());
        assertEquals(product.getCreatedOn(), updated.getCreatedOn());
        assertEquals("محصول ویرایش شده", updated.getTitle());
        assertFalse(updated.getEarlySettlementAllowed());
        assertEquals(1L, updated.getVersion()); assertNotNull(updated.getModifiedOn());
        assertEquals(204, client.delete().uri("/loan/products/{id}", id).retrieve().toBodilessEntity().getStatusCode().value());
        assertStatus(404, () -> client.get().uri("/loan/products/{id}", id).retrieve().toBodilessEntity());
    }

    @Test
    void rejectsInvalidFieldsRangesAndDates() {
        var client = client();
        for (String field : List.of("code", "productType", "earlySettlementAllowed")) {
            var invalid = request(); invalid.remove(field);
            assertStatus(400, () -> client.post().uri("/loan/products").body(invalid).retrieve().toBodilessEntity());
        }
        var longCode = request(); longCode.put("code", "X".repeat(21));
        assertStatus(400, () -> client.post().uri("/loan/products").body(longCode).retrieve().toBodilessEntity());
        var blank = request(); blank.put("code", "  ");
        assertStatus(400, () -> client.post().uri("/loan/products").body(blank).retrieve().toBodilessEntity());
        var negative = request(); negative.put("minAmount", -1);
        assertStatus(400, () -> client.post().uri("/loan/products").body(negative).retrieve().toBodilessEntity());
        var range = request(); range.put("minAmount", 10001);
        assertStatus(400, () -> client.post().uri("/loan/products").body(range).retrieve().toBodilessEntity());
        var counts = request(); counts.put("minInstallmentCount", 25);
        assertStatus(400, () -> client.post().uri("/loan/products").body(counts).retrieve().toBodilessEntity());
        var dates = request(); dates.put("fromDate", "2026-12-01T00:00:00"); dates.put("toDate", "2026-11-01T00:00:00");
        assertStatus(400, () -> client.post().uri("/loan/products").body(dates).retrieve().toBodilessEntity());
    }

    @Test
    void handlesDuplicateCodesAndMissingRecords() {
        var client = client();
        var first = request();
        var saved = client.post().uri("/loan/products").body(first).retrieve().body(ProductLoanResponseDto.class);
        assertStatus(409, () -> client.post().uri("/loan/products").body(first).retrieve().toBodilessEntity());
        var second = client.post().uri("/loan/products").body(request()).retrieve().body(ProductLoanResponseDto.class);
        assertStatus(409, () -> client.put().uri("/loan/products/{id}", second.getId()).body(first).retrieve().toBodilessEntity());
        assertEquals(saved.getCode(), client.get().uri("/loan/products/{id}", saved.getId()).retrieve().body(ProductLoanResponseDto.class).getCode());
        assertStatus(404, () -> client.put().uri("/loan/products/9999999").body(request()).retrieve().toBodilessEntity());
        assertStatus(404, () -> client.delete().uri("/loan/products/9999999").retrieve().toBodilessEntity());
    }

    @Test
    void hidesOtherTenantRecordsAndReportsGlobalUniqueCodeConflict() {
        var client = client();
        String code = "FOREIGN-" + UUID.randomUUID().toString().substring(0, 8);
        var jdbc = new JdbcTemplate(source);
        jdbc.update("insert into SHFQ_LOAN.PRODUCT_LOAN (CREATED_BY,CREATED_ON,MODIFIED_BY,MODIFIED_ON,ID,CODE,PRODUCT_TYPE,EARLY_SETTLEMENT_ALLOWED,FUND_ID,OPT_LOCK) values ('test',CURRENT_TIMESTAMP,'test',CURRENT_TIMESTAMP,?,?,?,?,?,?)",
                900000L, code, 0, true, 99, 0);
        assertStatus(404, () -> client.get().uri("/loan/products/900000").retrieve().toBodilessEntity());
        assertStatus(404, () -> client.put().uri("/loan/products/900000").body(request()).retrieve().toBodilessEntity());
        assertStatus(404, () -> client.delete().uri("/loan/products/900000").retrieve().toBodilessEntity());
        var page = client.get().uri("/loan/products?size=100").retrieve().body(tools.jackson.databind.JsonNode.class);
        for (var row : page.path("content")) assertEquals(43L, row.path("fundId").asLong());
        var duplicate = request(); duplicate.put("code", code);
        assertStatus(409, () -> client.post().uri("/loan/products").body(duplicate).retrieve().toBodilessEntity());
        assertEquals(1, jdbc.queryForObject("select count(*) from SHFQ_LOAN.PRODUCT_LOAN where ID=900000", Integer.class));
    }

    private RestClient client() { return RestClient.create("http://127.0.0.1:" + context.getWebServer().getPort()); }
    private Map<String, Object> request() {
        var request = new HashMap<String, Object>();
        request.put("code", "P-" + UUID.randomUUID().toString().substring(0, 10));
        request.put("title", "محصول تسهیلات"); request.put("productType", "ADI");
        request.put("status", "ACTIVE"); request.put("earlySettlementAllowed", true);
        request.put("minAmount", new BigDecimal("100.25")); request.put("maxAmount", new BigDecimal("10000.00"));
        request.put("minInstallmentCount", 1); request.put("maxInstallmentCount", 24); request.put("installmentInterval", 1);
        return request;
    }
    private void assertStatus(int status, Runnable action) {
        var exception = assertThrows(HttpClientErrorException.class, action::run);
        assertEquals(status, exception.getStatusCode().value());
        assertTrue(exception.getResponseBodyAsString().contains("detail"));
    }
}
