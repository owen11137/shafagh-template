package com.misc.shafagh.config;

import com.misc.shafagh.orchestration.service.api.AtomicProcessExecutor;
import com.misc.shafagh.orchestration.service.impl.AtomicProcessExecutorImpl;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** All runtime module aliases share one datasource, persistence unit and transaction manager. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "shafagh.persistence.module-datasources", havingValue = "false", matchIfMissing = true)
public class SinglePersistenceConfiguration {
    @Bean
    AtomicProcessExecutor atomicProcessExecutor() {
        return new AtomicProcessExecutorImpl();
    }

    @Bean
    static BeanFactoryPostProcessor moduleTransactionAliases() {
        return factory -> {
            factory.registerAlias("transactionManager", "appTransactionManager");
            for (String module : new String[]{"base", "cif", "dpst", "loan"}) {
                factory.registerAlias("dataSource", module + "DataSource");
                factory.registerAlias("transactionManager", module + "TransactionManager");
                factory.registerAlias("entityManagerFactory", module + "EntityManagerFactory");
            }
        };
    }
}
