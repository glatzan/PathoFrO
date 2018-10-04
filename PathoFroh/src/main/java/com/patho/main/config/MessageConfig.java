package com.patho.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.patho.main.config.util.ResourceBundle;

@Configuration
public class MessageConfig {

	@Bean
	public ResourceBundle resourceBundle() {
		return new ResourceBundle();
	}
}
