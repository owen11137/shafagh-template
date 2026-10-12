package com.misc.shafagh.cif.impl.config.security.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author m_kor
 * date 4/29/2026
 */
@Component
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Value("${keycloak-management.client-id:}")
    private String client_id;
    private static final String RESOURCE_ACCESS = "resource_access";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Object resourceAccessClaim = source.getClaims().get(RESOURCE_ACCESS);
        if (!(resourceAccessClaim instanceof Map<?, ?> resourceAccess) || client_id.isBlank()) {
            return List.of();
        }
        Object clientAccessClaim = resourceAccess.get(client_id);
        if (!(clientAccessClaim instanceof Map<?, ?> realmAccess)) {
            return List.of();
        }

        if (realmAccess.isEmpty() || !(realmAccess.get(ROLES_CLAIM) instanceof List<?> roles)) {
            return new ArrayList<>();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(roleName -> "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
