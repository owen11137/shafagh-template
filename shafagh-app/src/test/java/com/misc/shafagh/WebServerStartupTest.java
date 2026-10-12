package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebServerStartupTest {

    @Autowired private ServletWebServerApplicationContext applicationContext;

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
            // The template has no controller for this route, so MVC returns a real HTTP 404.
            assertEquals(404, response.statusCode());
        }
    }
}
