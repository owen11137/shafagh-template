package com.misc.shafagh.cif.impl.config.security.keycloak;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @author m_kor
 * date 2/8/2025
 */
@Configuration
@ConditionalOnProperty(prefix = "keycloak-management", name = {"client-id", "client-secret"})
@AllArgsConstructor
public class KeyLockRestTemplateFactory {
    private final KeyLockRestTemplateInterceptor keylockRestTemplateInterceptor;
    @Bean
    public RestTemplate getKeyLockRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
//        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
//        if (CollectionUtils.isEmpty(interceptors)) {
//            interceptors = new ArrayList<>();
//        }
//        interceptors.add(keylockRestTemplateInterceptor);
//        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
