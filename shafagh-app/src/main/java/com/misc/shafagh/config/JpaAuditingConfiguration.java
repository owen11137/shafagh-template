package com.misc.shafagh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables creation/modification timestamps on the shared persistence base class. */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(auditorAwareRef="currentUserAuditor")
public class JpaAuditingConfiguration {
    @org.springframework.context.annotation.Bean
    org.springframework.data.domain.AuditorAware<String> currentUserAuditor(
            com.misc.shafagh.common.context.CurrentUserContext currentUserContext) {
        return () -> java.util.Optional.of(Long.toString(currentUserContext.userId()));
    }
}
