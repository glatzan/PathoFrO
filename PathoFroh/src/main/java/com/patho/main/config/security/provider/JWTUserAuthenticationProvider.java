package com.patho.main.config.security.provider;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.patho.main.config.security.util.JWTAuthorizationToken;
import com.patho.main.config.security.util.JWTPasswortApplicationToken;
import com.patho.main.model.user.HistoUser;
import com.patho.main.service.AuthenticationService;
import com.patho.main.util.ldap.LDAPAuthenticationException;

import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "patho.jwt")
@Setter
public class JWTUserAuthenticationProvider implements AuthenticationProvider {

	private String secret;

	@Autowired
	private AuthenticationService authenticationService;

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		JWTPasswortApplicationToken auth = (JWTPasswortApplicationToken) authentication;

		Optional<HistoUser> user;
		try {
			user = authenticationService.authenticate(auth.getPrincipal().toString(), auth.getCredentials().toString());
		} catch (LDAPAuthenticationException e) {
			// authentication was not successful, but user is in database
			throw new BadCredentialsException("Invalid Password or User");
		}

		return new JWTAuthorizationToken(AuthenticationService.generateJWTToken(user.get(), secret));
	}

	public boolean supports(Class<?> authentication) {
		if (authentication.isAssignableFrom(JWTPasswortApplicationToken.class)) {
			return true;
		}
		return false;
	}
}