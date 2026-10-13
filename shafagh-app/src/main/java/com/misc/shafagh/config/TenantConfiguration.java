package com.misc.shafagh.config;
import com.misc.shafagh.config.persistence.FixedFundTenantIdentifierResolver;
import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/** Shared-table tenant isolation for entities inheriting TenantAwareEntity, across all persistence units. */
@Configuration(proxyBeanMethods=false)
public class TenantConfiguration {
    @Bean
    CurrentTenantIdentifierResolver<Long> fundTenantIdentifierResolver(@Value("${shafagh.fund-id:1}") long fundId) {
        return new FixedFundTenantIdentifierResolver(fundId);
    }
    @Bean
    HibernatePropertiesCustomizer tenantIdentifierCustomizer(CurrentTenantIdentifierResolver<Long> resolver) {
        return properties -> properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }
}
