package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebServerStartupTest {

    @Autowired private ServletWebServerApplicationContext applicationContext;
    @Value("${shafagh.client-url:http://localhost:3000}") private String clientUrl;

    @Test
    void embeddedServerStartsAndHandlesHttpRequests() throws Exception {
        int port = applicationContext.getWebServer().getPort();
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port + "/__startup_probe__"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            // No controller handles this route; an anonymous request reaches MVC and gets 404.
            assertEquals(404, response.statusCode());
        }
    }

    @Test
    void configuredFrontendCanMakePreflightRequestsWithoutAuthentication() throws Exception {
        int port = applicationContext.getWebServer().getPort();
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port + "/customer/real/save"))
                .timeout(Duration.ofSeconds(10))
                .header("Origin", clientUrl)
                .header("Access-Control-Request-Method", "POST")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            assertEquals(200, response.statusCode());
            assertEquals(clientUrl, response.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
        }
    }
    @Test
    void baseControllersAreRegisteredAndReturnNotFoundForMissingIds() throws Exception {
        int port = applicationContext.getWebServer().getPort();
        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String resource : new String[]{"city", "fund", "branch"}) {
                HttpRequest list = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/base/" + resource))
                        .timeout(Duration.ofSeconds(10)).GET().build();
                assertEquals(200, client.send(list, HttpResponse.BodyHandlers.discarding()).statusCode());
                HttpRequest missing = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/base/" + resource + "/-1"))
                        .timeout(Duration.ofSeconds(10)).GET().build();
                assertEquals(404, client.send(missing, HttpResponse.BodyHandlers.discarding()).statusCode());
            }
        }
    }
    @Test
    void baseRestEndpointsCreateAndUpdateRecordsWithCifOwner() {
        var client = org.springframework.web.client.RestClient.create(
                "http://127.0.0.1:" + applicationContext.getWebServer().getPort());
        var city = new com.misc.shafagh.base.api.dto.CityDto();
        city.setName("تهران"); city.setParentId(0L);
        city = client.post().uri("/base/city").body(city).retrieve()
                .body(com.misc.shafagh.base.api.dto.CityDto.class);
        var customer = new com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO();
        customer.setIdentifier(java.util.UUID.randomUUID().toString());
        customer.setCustomerType(com.misc.shafagh.cif.impl.entity.enums.CustomerType.REAL);
        customer.setRegistrationDate(new java.util.Date()); customer.setCityId(city.getId());
        customer.setFirstName("علی"); customer.setLastName("رضایی"); customer.setFatherName("محمد");
        customer.setGender(com.misc.shafagh.cif.impl.entity.enums.Gender.MALE);
        customer.setMaritalStatus(com.misc.shafagh.cif.impl.entity.enums.MaritalStatus.SINGLE);
        var address = new com.misc.shafagh.cif.impl.dto.request.CustomerAddressReqDTO();
        address.setCityId(city.getId()); address.setAddress("نشانی");
        address.setAddressType(com.misc.shafagh.cif.impl.entity.enums.AddressType.HOME);
        var contact = new com.misc.shafagh.cif.impl.dto.request.CustomerContactReqDTO();
        contact.setMobileNumber("09120000000");
        customer.setAddresses(java.util.List.of(address)); customer.setContacts(java.util.List.of(contact));
        var owner = client.post().uri("/customer/real/save").body(customer).retrieve()
                .body(com.misc.shafagh.cif.impl.dto.response.CustomerDto.class);
        var fund = new com.misc.shafagh.base.api.dto.FundDto();
        fund.setCode("F-" + java.util.UUID.randomUUID()); fund.setName("صندوق"); fund.setOwnerId(owner.getId());
        fund = client.post().uri("/base/fund").body(fund).retrieve()
                .body(com.misc.shafagh.base.api.dto.FundDto.class);
        var branch = new com.misc.shafagh.base.api.dto.BranchDto();
        branch.setCode("B1"); branch.setName("شعبه"); branch.setFundId(fund.getId());
        branch = client.post().uri("/base/branch").body(branch).retrieve()
                .body(com.misc.shafagh.base.api.dto.BranchDto.class);
        city.setName("شهر ویرایش شده"); fund.setName("صندوق ویرایش شده"); branch.setName("شعبه ویرایش شده");
        assertEquals(city.getName(), client.put().uri("/base/city/{id}", city.getId()).body(city).retrieve()
                .body(com.misc.shafagh.base.api.dto.CityDto.class).getName());
        assertEquals(fund.getName(), client.put().uri("/base/fund/{id}", fund.getId()).body(fund).retrieve()
                .body(com.misc.shafagh.base.api.dto.FundDto.class).getName());
        assertEquals(branch.getName(), client.put().uri("/base/branch/{id}", branch.getId()).body(branch).retrieve()
                .body(com.misc.shafagh.base.api.dto.BranchDto.class).getName());
        assertEquals(city.getName(), client.post().uri("/customer/real/getById/{id}", owner.getId()).retrieve()
                .body(com.misc.shafagh.cif.impl.dto.response.CustomerDto.class).getCity().getName());
    }
    @Test
    void sharedSwaggerServesUiAndDocumentsAllCurrentModuleControllers() throws Exception {
        int port = applicationContext.getWebServer().getPort();
        try (HttpClient client = HttpClient.newHttpClient()) {
            var specificationRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v3/api-docs"))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            var specification = client.send(specificationRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, specification.statusCode());
            var document = tools.jackson.databind.json.JsonMapper.builder().build().readTree(specification.body());
            assertEquals("Shafagh API", document.path("info").path("title").asText());
            for (String path : new String[]{"/base/city", "/base/fund", "/base/branch", "/customer/real/save", "/customer/legal/save"}) {
                org.junit.jupiter.api.Assertions.assertTrue(document.path("paths").has(path), path);
            }
            org.junit.jupiter.api.Assertions.assertTrue(document.path("paths").path("/base/city").has("post"));
            org.junit.jupiter.api.Assertions.assertTrue(document.path("components").path("schemas").has("CityDto"));
            for (String path : new String[]{"/swagger-ui/index.html", "/swagger-ui/swagger-ui-bundle.js", "/v3/api-docs/swagger-config"}) {
                var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                        .timeout(Duration.ofSeconds(10)).GET().build();
                assertEquals(200, client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode(), path);
            }
        }
    }
    @Test
    void swaggerDocumentsOperationsModelsAndSeparatesModuleGroups() throws Exception {
        String root = "http://127.0.0.1:" + applicationContext.getWebServer().getPort();
        var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(URI.create(root + "/v3/api-docs"))
                    .timeout(Duration.ofSeconds(10)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            var document = mapper.readTree(response.body());
            int operationCount = 0;
            for (var path : document.path("paths")) {
                for (String method : new String[]{"get", "post", "put", "delete", "patch"}) {
                    if (!path.has(method)) continue;
                    var operation = path.path(method);
                    org.junit.jupiter.api.Assertions.assertFalse(operation.path("summary").asText().isBlank());
                    org.junit.jupiter.api.Assertions.assertFalse(operation.path("description").asText().isBlank());
                    org.junit.jupiter.api.Assertions.assertTrue(operation.path("tags").size() > 0);
                    operationCount++;
                }
            }
            assertEquals(21, operationCount);
            for (String model : new String[]{"CityDto", "FundDto", "BranchDto", "CustomerReqDTO", "CustomerDto", "CustomerFilterDTO"}) {
                org.junit.jupiter.api.Assertions.assertFalse(document.path("components").path("schemas").path(model)
                        .path("description").asText().isBlank(), model);
            }
            org.junit.jupiter.api.Assertions.assertTrue(document.path("components").path("schemas").path("FundDto")
                    .path("properties").path("ownerId").path("description").asText().contains("CIF"));
            for (String group : new String[]{"00-all", "01-base", "02-cif", "03-dpst", "04-loan"}) {
                var groupedResponse = client.send(HttpRequest.newBuilder(URI.create(root + "/v3/api-docs/" + group))
                        .timeout(Duration.ofSeconds(10)).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, groupedResponse.statusCode(), group);
                var grouped = mapper.readTree(groupedResponse.body());
                if (group.equals("00-all")) assertEquals(document.path("paths").size(), grouped.path("paths").size());
                if (group.equals("01-base")) {
                    org.junit.jupiter.api.Assertions.assertTrue(grouped.path("paths").has("/base/city"));
                    org.junit.jupiter.api.Assertions.assertFalse(grouped.path("paths").has("/customer/real/save"));
                    assertEquals("ثبت شهر جدید", grouped.path("paths").path("/base/city").path("post").path("summary").asText());
                }
                if (group.equals("02-cif")) {
                    org.junit.jupiter.api.Assertions.assertTrue(grouped.path("paths").has("/customer/real/save"));
                    org.junit.jupiter.api.Assertions.assertFalse(grouped.path("paths").has("/base/city"));
                }
                if (group.equals("03-dpst") || group.equals("04-loan")) assertEquals(0, grouped.path("paths").size());
            }
        }
    }
}
