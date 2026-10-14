package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import com.misc.shafagh.config.SpringSecurityAuditorAware;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SpringSecurityAuditorAwareTest {
    private final SpringSecurityAuditorAware auditor = new SpringSecurityAuditorAware("system");

    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test void usesDefaultWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();
        assertEquals("system", auditor.getCurrentAuditor().orElseThrow());
    }

    @Test void usesAuthenticatedPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("operator-42", null, List.of()));
        assertEquals("operator-42", auditor.getCurrentAuditor().orElseThrow());
    }

    @Test void ignoresAnonymousAndUnauthenticatedPrincipals() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        assertEquals("system", auditor.getCurrentAuditor().orElseThrow());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.unauthenticated("unverified", null));
        assertEquals("system", auditor.getCurrentAuditor().orElseThrow());
    }

    @Test void supportsConfiguredDefaultAndRejectsInvalidDefaults() {
        assertEquals("batch", new SpringSecurityAuditorAware("batch").getCurrentAuditor().orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> new SpringSecurityAuditorAware(" "));
        assertThrows(IllegalArgumentException.class, () -> new SpringSecurityAuditorAware(null));
    }
}
