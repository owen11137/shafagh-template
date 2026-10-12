package com.misc.shafagh.cif.impl.config.security.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author m_kor
 * date 2/8/2025
 */
@Component
@ConditionalOnProperty(prefix = "keycloak-management", name = {"client-id", "client-secret"})
public class KeyLockRestTemplateInterceptor implements ClientHttpRequestInterceptor {
    @Value("${keycloak-management.client-id}")
    private String clientId;
    @Value("${keycloak-management.client-secret}")
    private String clientSecret;
    public static final String BASIC_AUTHORIZATION = "Basic ";
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response;
        request.getHeaders().add(HttpHeaders.AUTHORIZATION,  BASIC_AUTHORIZATION + HttpHeaders.encodeBasicAuth(clientId, clientSecret, StandardCharsets.UTF_8));
        response = execution.execute(request, body);
        return response;
    }
}
