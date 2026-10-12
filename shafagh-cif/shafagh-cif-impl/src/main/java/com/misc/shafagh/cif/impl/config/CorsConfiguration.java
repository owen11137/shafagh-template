package com.misc.shafagh.cif.impl.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration(proxyBeanMethods = false)
public class CorsConfiguration {
    @Bean
    CorsFilter corsFilter(@Qualifier("uiCorsConfig") CorsConfigurationSource source) {
        return new CorsFilter(source);
    }
}
