package com.misc.shafagh.config;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/** Oracle development schema support; does not assign or rename database object names. */
@Configuration(proxyBeanMethods=false)
public class OracleSchemaConfiguration {
    @Bean
    HibernatePropertiesCustomizer oracleSchemaDialect() {
        return properties -> properties.put("hibernate.dialect_resolvers", ShafaghOracleDialectResolver.class.getName());
    }
}
