package com.patho.main;

import com.patho.main.config.*;
import com.patho.main.repository.jpa.impl.BaseRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.patho.main"})
@EnableScheduling
@EnableConfigurationProperties
@Import({MessageConfig.class, DatabaseConfig.class, LdapConfig.class, ThreadConfig.class})
@EntityScan(basePackages = {"com.patho.main.model"})
@EnableLdapRepositories(basePackages = {"com.patho.main.repository.miscellaneous"})
@EnableJpaRepositories(basePackages = {"com.patho.main.repository.jpa"},
        repositoryBaseClass = BaseRepositoryImpl.class
)
public class PathoFrohApplication {
    public static void main(String[] args) {
        SpringApplication.run(PathoFrohApplication.class, args);
    }
}

