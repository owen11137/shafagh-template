package com.misc.shafagh.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Single datasource mode retained for the existing shared H2 integration suite. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "shafagh.persistence.module-datasources", havingValue = "false")
public class SinglePersistenceConfiguration {
    @Bean
    static BeanFactoryPostProcessor moduleTransactionAliases() {
        return factory -> {
            for (String module : new String[]{"base", "cif", "dpst", "loan"}) {
                factory.registerAlias("transactionManager", module + "TransactionManager");
            }
        };
    }
}
