package com.misc.shafagh.cif.impl.config.security;

import com.misc.shafagh.cif.impl.config.security.keycloak.KeycloakRealmRoleConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * @author m_kor
 * date 1/25/2025
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;
    private final KeycloakRealmRoleConverter keycloakRealmRoleConverter;

    @Autowired
    public SecurityConfig(@Qualifier("uiCorsConfig") CorsConfigurationSource corsConfigurationSource, KeycloakRealmRoleConverter keycloakRealmRoleConverter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.keycloakRealmRoleConverter = keycloakRealmRoleConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(keycloakRealmRoleConverter);

        http.cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource));
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(registry -> registry
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/configuration/**",
                        "user/**"
                ).permitAll()
                .anyRequest().permitAll()
        ).oauth2ResourceServer(rsc -> rsc.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }
}

