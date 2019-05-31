package com.patho.main.config;

import com.patho.main.config.util.ResourceBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {

    @Bean
    public ResourceBundle resourceBundle(@Autowired ResourceBundle resourceBundle) {
        return resourceBundle;
    }

}
