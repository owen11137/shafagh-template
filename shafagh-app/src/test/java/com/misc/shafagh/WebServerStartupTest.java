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
}
