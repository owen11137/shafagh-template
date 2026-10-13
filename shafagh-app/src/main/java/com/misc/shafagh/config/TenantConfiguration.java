package com.misc.shafagh.config;

import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * One fixed fund per application process. Only entities annotated with
 * {@code @TenantId} are scoped; entities without it remain shared.
 */
@Configuration(proxyBeanMethods = false)
public class TenantConfiguration {

    @Bean
    HibernatePropertiesCustomizer tenantIdentifierCustomizer(@Value("${shafagh.fund-id:1}") long fundId) {
        if (fundId <= 0) {
            throw new IllegalArgumentException("shafagh.fund-id must be positive");
        }

        CurrentTenantIdentifierResolver<Long> resolver = new CurrentTenantIdentifierResolver<>() {
            @Override
            public Long resolveCurrentTenantIdentifier() {
                return fundId;
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };

        return properties -> properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }
}
