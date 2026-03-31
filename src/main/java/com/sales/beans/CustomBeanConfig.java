package com.sales.beans;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomBeanConfig {


    @Bean("safeLogger")
    public Logger getLogger() {
        return LoggerFactory.getLogger("test");
    }

}
