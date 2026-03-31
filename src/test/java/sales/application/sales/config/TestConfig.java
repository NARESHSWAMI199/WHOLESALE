package sales.application.sales.config;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sales.application.sales.admin.controller.UserControllerTest;


@Configuration
public class TestConfig {


    @Bean
    public UserControllerTest getUserController() {
        return new UserControllerTest();
    }


    @Bean
    public Logger getLogger() {
        return LogManager.getLogger(this);
    }


}
