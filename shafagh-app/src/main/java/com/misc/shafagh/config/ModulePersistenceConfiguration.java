package com.misc.shafagh.config;

import com.zaxxer.hikari.HikariDataSource;
import com.misc.shafagh.common.SchemaName;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/** Each module connects as its own Oracle schema owner and scans only its entities. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "shafagh.persistence.module-datasources", havingValue = "true", matchIfMissing = true)
public class ModulePersistenceConfiguration {
    private final Environment environment;
    private final ObjectProvider<HibernatePropertiesCustomizer> customizers;

    public ModulePersistenceConfiguration(Environment environment,
            ObjectProvider<HibernatePropertiesCustomizer> customizers) {
        this.environment = environment;
        this.customizers = customizers;
    }

    private HikariDataSource dataSource(String module) {
        String prefix = "shafagh.persistence." + module + ".";
        HikariDataSource source = new HikariDataSource();
        source.setPoolName("shafagh-" + module);
        source.setJdbcUrl(environment.getRequiredProperty(prefix + "url"));
        source.setUsername(environment.getRequiredProperty(prefix + "username"));
        source.setPassword(environment.getRequiredProperty(prefix + "password"));
        source.setDriverClassName(environment.getProperty(prefix + "driver-class-name", "oracle.jdbc.OracleDriver"));
        source.setMaximumPoolSize(environment.getProperty(prefix + "maximum-pool-size", Integer.class, 5));
        return source;
    }

    private LocalContainerEntityManagerFactoryBean entityManagerFactory(String module, DataSource source) {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(source);
        factory.setPersistenceUnitName(module);
        factory.setPackagesToScan("com.misc.shafagh." + module + ".impl");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.default_schema", switch (module) {
            case "base" -> SchemaName.BASE;
            case "cif" -> SchemaName.CIF;
            case "dpst" -> SchemaName.DP;
            case "loan" -> SchemaName.LN;
            default -> throw new IllegalArgumentException("Unknown persistence module: " + module);
        });
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("spring.jpa.hibernate.ddl-auto", "update"));
        // Fail startup when schema permissions or DDL are wrong, rather than silently losing tables.
        properties.put("hibernate.hbm2ddl.halt_on_error", true);
        properties.put("hibernate.show_sql", environment.getProperty("spring.jpa.show-sql", "false"));
        customizers.orderedStream().forEach(customizer -> customizer.customize(properties));
        factory.setJpaPropertyMap(properties);
        return factory;
    }

    @Bean(name = "baseDataSource", destroyMethod = "close")
    HikariDataSource baseDataSource() { return dataSource("base"); }

    @Bean(name = "baseEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean baseEntityManagerFactory(
            @Qualifier("baseDataSource") DataSource source) {
        return entityManagerFactory("base", source);
    }

    @Bean(name = "baseTransactionManager")
    JpaTransactionManager baseTransactionManager(
            @Qualifier("baseEntityManagerFactory") jakarta.persistence.EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "shafagh.persistence.module-datasources", havingValue = "true", matchIfMissing = true)
    @EnableJpaRepositories(basePackages = "com.misc.shafagh.base.impl.repository",
            entityManagerFactoryRef = "baseEntityManagerFactory", transactionManagerRef = "baseTransactionManager")
    static class BaseRepositories {}

    @Bean(name = {"cifDataSource", "dataSource"}, destroyMethod = "close")
    @Primary
    HikariDataSource cifDataSource() { return dataSource("cif"); }

    @Bean(name = {"cifEntityManagerFactory", "entityManagerFactory"})
    @Primary
    LocalContainerEntityManagerFactoryBean cifEntityManagerFactory(
            @Qualifier("cifDataSource") DataSource source) {
        return entityManagerFactory("cif", source);
    }

    @Bean(name = {"cifTransactionManager", "transactionManager"})
    @Primary
    JpaTransactionManager cifTransactionManager(
            @Qualifier("cifEntityManagerFactory") jakarta.persistence.EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "shafagh.persistence.module-datasources", havingValue = "true", matchIfMissing = true)
    @EnableJpaRepositories(basePackages = "com.misc.shafagh.cif.impl.repository",
            entityManagerFactoryRef = "cifEntityManagerFactory", transactionManagerRef = "cifTransactionManager")
    static class CifRepositories {}

    @Bean(name = "dpstDataSource", destroyMethod = "close")
    HikariDataSource dpstDataSource() { return dataSource("dpst"); }

    @Bean(name = "dpstEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean dpstEntityManagerFactory(
            @Qualifier("dpstDataSource") DataSource source) {
        return entityManagerFactory("dpst", source);
    }

    @Bean(name = "dpstTransactionManager")
    JpaTransactionManager dpstTransactionManager(
            @Qualifier("dpstEntityManagerFactory") jakarta.persistence.EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "shafagh.persistence.module-datasources", havingValue = "true", matchIfMissing = true)
    @EnableJpaRepositories(basePackages = "com.misc.shafagh.dpst.impl.repository",
            entityManagerFactoryRef = "dpstEntityManagerFactory", transactionManagerRef = "dpstTransactionManager")
    static class DpstRepositories {}

    @Bean(name = "loanDataSource", destroyMethod = "close")
    HikariDataSource loanDataSource() { return dataSource("loan"); }

    @Bean(name = "loanEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean loanEntityManagerFactory(
            @Qualifier("loanDataSource") DataSource source) {
        return entityManagerFactory("loan", source);
    }

    @Bean(name = "loanTransactionManager")
    JpaTransactionManager loanTransactionManager(
            @Qualifier("loanEntityManagerFactory") jakarta.persistence.EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "shafagh.persistence.module-datasources", havingValue = "true", matchIfMissing = true)
    @EnableJpaRepositories(basePackages = "com.misc.shafagh.loan.impl.repository",
            entityManagerFactoryRef = "loanEntityManagerFactory", transactionManagerRef = "loanTransactionManager")
    static class LoanRepositories {}
}
