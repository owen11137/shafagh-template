package com.misc.shafagh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Uses the authenticated principal or a configured technical actor for JPA auditing. */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(auditorAwareRef = "applicationAuditor")
public class JpaAuditingConfiguration {
    @Bean
    AuditorAware<String> applicationAuditor(@Value("${shafagh.audit.actor:system}") String actor) {
        return new SpringSecurityAuditorAware(actor);
    }
}
