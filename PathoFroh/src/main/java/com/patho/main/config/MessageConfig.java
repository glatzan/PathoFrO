package com.patho.main.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.patho.main.config.util.ResourceBundle;

@Configuration
public class MessageConfig {

	@Bean
	public ResourceBundle resourceBundle() {
		return new ResourceBundle();
	}
}
