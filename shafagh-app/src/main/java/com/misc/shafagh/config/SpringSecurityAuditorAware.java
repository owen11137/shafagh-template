package com.misc.shafagh.config;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Resolves the authenticated user; development and anonymous execution use the fallback actor. */
public final class SpringSecurityAuditorAware implements AuditorAware<String> {
    private final String defaultActor;

    public SpringSecurityAuditorAware(String defaultActor) {
        if (defaultActor == null || defaultActor.isBlank() || defaultActor.length() > 255) {
            throw new IllegalArgumentException("Default audit actor must contain 1 to 255 characters");
        }
        this.defaultActor = defaultActor;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String name = authentication.getName();
            if (name != null && !name.isBlank()) {
                return Optional.of(name);
            }
        }
        return Optional.of(defaultActor);
    }
}
