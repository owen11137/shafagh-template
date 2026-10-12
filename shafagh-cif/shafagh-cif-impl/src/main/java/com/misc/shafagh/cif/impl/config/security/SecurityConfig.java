package com.misc.shafagh.cif.impl.config.security;

import com.misc.shafagh.cif.impl.config.security.keycloak.KeycloakRealmRoleConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<JwtDecoder> jwtDecoders) throws Exception {
        JwtDecoder jwtDecoder = jwtDecoders.getIfAvailable();
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));
        http.csrf(AbstractHttpConfigurer::disable);

        if (jwtDecoder == null) {
            // Do not expose endpoints without an active, configured JWT verifier.
            http.authorizeHttpRequests(registry -> registry
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                    .anyRequest().denyAll());
        } else {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            converter.setJwtGrantedAuthoritiesConverter(keycloakRealmRoleConverter);
            http.authorizeHttpRequests(registry -> registry
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml", "/configuration/**").permitAll()
                    .anyRequest().authenticated());
            http.oauth2ResourceServer(resource -> resource.jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(converter)));
        }
        return http.build();
    }
}
