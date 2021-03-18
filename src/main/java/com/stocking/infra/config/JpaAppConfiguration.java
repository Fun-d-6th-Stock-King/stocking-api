package com.stocking.infra.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.stocking.modules", entityManagerFactoryRef = "appEntityManager", transactionManagerRef = "appTransactionManager")
@ConfigurationProperties("spring.datasource.hikari")
@EnableTransactionManagement
public class JpaAppConfiguration extends HikariConfig {

    @Value("${hibernate.dialect}")
    private String dialect;

    @Value("${hibernate.showSql}")
    private String showSql;

    @Value("${hibernate.formatSql}")
    private String formatSql;

    @Value("${hibernate.hbm2ddlAuto}")
    private String hbm2ddlAuto;

    @Bean(name = "appDataSource")
    @Qualifier("appDataSource")
    public HikariDataSource appDataSource() {
        return new HikariDataSource(this);
    }

    @Bean(name = "appEntityManager")
    public LocalContainerEntityManagerFactoryBean appEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(appDataSource());
        em.setPackagesToScan("com.stocking.modules");
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", dialect);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        em.setJpaPropertyMap(properties);
        em.setJpaVendorAdapter(vendorAdapter);
        em.setPersistenceUnitName("app");
        em.setPersistenceUnitName("appEntityManager");

        return em;
    }

    @Bean(name = "appTransactionManager")
    public PlatformTransactionManager appTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(appEntityManager().getObject());
        return transactionManager;
    }

    @Bean(name = "appExceptionTranslation")
    public PersistenceExceptionTranslationPostProcessor appExceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}