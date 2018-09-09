package com.patho.main.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "patho.ldap")
@Getter
@Setter
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
