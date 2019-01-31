package com.patho.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.patho.main.config.DatabaseConfig;
import com.patho.main.config.LdapConfig;
import com.patho.main.config.MessageConfig;

@SpringBootApplication
@EnableSpringConfigured
@ComponentScan
@EnableScheduling
@Import({ MessageConfig.class, DatabaseConfig.class, LdapConfig.class })
public class PathoFrohApplication {

	public static void main(String[] args) {
		SpringApplication.run(PathoFrohApplication.class, args);
	}
}
