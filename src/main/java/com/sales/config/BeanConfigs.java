package com.sales.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
public class BeanConfigs {

    @Bean("safeLogger")
    public Logger getLogger() {
        return LoggerFactory.getLogger("test");
    }

    @Bean
    public NewTopic notificationTopic(){
        return TopicBuilder.name("notification-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
