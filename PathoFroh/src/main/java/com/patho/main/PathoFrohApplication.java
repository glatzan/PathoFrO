package com.patho.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.test.context.web.WebAppConfiguration;

import com.patho.main.config.DatabaseConfig;
import com.patho.main.config.LdapConfig;
import com.patho.main.config.MessageConfig;

@SpringBootApplication
@EnableLoadTimeWeaving(aspectjWeaving = AspectJWeaving.ENABLED)
@EnableSpringConfigured
@WebAppConfiguration
@ComponentScan
@Import({ MessageConfig.class, DatabaseConfig.class, LdapConfig.class })
public class PathoFrohApplication {

	public static void main(String[] args) {
		SpringApplication.run(PathoFrohApplication.class, args);
	}
}
