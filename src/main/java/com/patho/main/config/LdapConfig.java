package com.patho.main.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@ConfigurationProperties(prefix = "patho.ldap")
@Getter
@Setter
@EnableLdapRepositories(basePackages="com.patho.main.repository.miscellaneous")
public class LdapConfig {

    private String ldapUrl;

    private String baseDn;

    @Bean
    public LdapContextSource getContextSource() throws Exception {
        System.out.println(ldapUrl + " " + baseDn);
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(ldapUrl);
        ldapContextSource.setBase(baseDn);
        return ldapContextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() throws Exception {
        LdapTemplate ldapTemplate = new LdapTemplate(getContextSource());
        ldapTemplate.setIgnorePartialResultException(true);
        ldapTemplate.setContextSource(getContextSource());
        return ldapTemplate;
    }
}
