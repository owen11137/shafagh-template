package com.misc.shafagh.config.persistence;

import java.util.List;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;

@Configuration(proxyBeanMethods = false)
public class DatabaseNamingConfiguration {
    @Bean
    HibernatePropertiesCustomizer databaseObjectNames() {
        return properties -> {
            properties.put("hibernate.implicit_naming_strategy", new DatabaseNamingStrategy());
            properties.put("hibernate.integrator_provider", (IntegratorProvider) () -> List.of(new ConstraintNamingIntegrator()));
            properties.put("hibernate.dialect_resolvers", ShafaghOracleDialectResolver.class.getName());
        };
    }
}
