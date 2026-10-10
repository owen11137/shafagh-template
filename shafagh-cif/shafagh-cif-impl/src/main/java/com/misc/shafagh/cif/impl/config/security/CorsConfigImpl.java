package com.misc.shafagh.cif.impl.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

/**
 * @author m_kor
 * date 2/15/2025
 */
@Component("uiCorsConfig")
public class CorsConfigImpl implements CorsConfigurationSource {
    @Value("${shafagh.client-url}")
    private String clientUrl;
    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Collections.singletonList(clientUrl));
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true); // enable accept user credential or any other applicable cookie from UI
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
        corsConfiguration.setMaxAge(3600L);
        return corsConfiguration;
    }
}
