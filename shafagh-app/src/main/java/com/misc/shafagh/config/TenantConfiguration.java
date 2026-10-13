package com.misc.shafagh.config;

import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import com.misc.shafagh.common.context.CurrentUserContext;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fund resolved from the shared current-user context for each Hibernate session. Only entities annotated with
 * {@code @TenantId} are scoped; entities without it remain shared.
 */
@Configuration(proxyBeanMethods = false)
public class TenantConfiguration {

    @Bean
    HibernatePropertiesCustomizer tenantIdentifierCustomizer(CurrentUserContext currentUserContext) {
        CurrentTenantIdentifierResolver<Long> resolver = new CurrentTenantIdentifierResolver<>() {
            @Override
            public Long resolveCurrentTenantIdentifier() {
                return currentUserContext.fundId();
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };

        return properties -> properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }
}
