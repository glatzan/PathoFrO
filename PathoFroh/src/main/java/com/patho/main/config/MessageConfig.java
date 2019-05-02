package com.patho.main.config;

import org.atmosphere.cpr.ContainerInitializer;
import org.primefaces.push.PushServlet;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.patho.main.config.util.ResourceBundle;
import org.springframework.core.Ordered;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collections;

@Configuration
public class MessageConfig {

	@Bean
	public ResourceBundle resourceBundle() {
		return new ResourceBundle();
	}

}
