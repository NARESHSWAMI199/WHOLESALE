package com.sales.config.auth;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

//@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "salesEntityManagerFactory",
        basePackages = {"com.sales.repositories"},
        transactionManagerRef = "salesTransactionManager"

)

public class SalesConfig {


    //	create a DataSource
    @Primary
    @Bean(name = "salesDatasource")
    @ConfigurationProperties(prefix = "spring.sales.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    //	Entity Manger Factory
    @Primary
    @Bean(name = "salesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(EntityManagerFactoryBuilder builder, @Qualifier("salesDatasource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        return builder.dataSource(dataSource).properties(properties).packages("com.sales.entities").persistenceUnit("Sales").build();
    }


    // creating Transaction Manger
    @Primary
    @Bean(name = "salesTransactionManager")
    public PlatformTransactionManager blogTransactionManager(
            @Qualifier("salesEntityManagerFactory") EntityManagerFactory blogEntityManagerFactory) {
        return new JpaTransactionManager(blogEntityManagerFactory);
    }

}