package com.patho.main.config.security.provider;

import com.patho.main.config.security.util.JWTAuthorizationToken;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.UserRepository;
import com.patho.main.service.AuthenticationService;
import com.patho.main.util.helper.HistoUtil;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConfigurationProperties(prefix = "patho.jwt")
@Setter
public class JWTAuthenticationProvider implements AuthenticationProvider {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String tokenPrefix;

	private String secret;

	@Autowired
	private UserRepository userRepository;

	public JWTAuthenticationProvider() {
	}

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		JWTAuthorizationToken auth = (JWTAuthorizationToken) authentication;

		String username = AuthenticationService.verifyJWTToken(auth, secret, tokenPrefix);

		if (HistoUtil.isNullOrEmpty(username)) {
			logger.debug("Bad JWT Token! Access denied");
			throw new BadCredentialsException("Invalid Token");
		}

		Optional<HistoUser> user = userRepository.findOptionalByPhysicianUid(username);

		logger.debug("Looking for User " + username);

		if (user.isPresent()) {
			logger.debug("Token OK! Access granted");
			auth.setUser(user.get());
			auth.setAuthenticated(true);
		} else {
			logger.debug("No User found! Access denied");
			throw new BadCredentialsException("No User found");
		}

		return auth;
	}

	public boolean supports(Class<?> authentication) {
		if (authentication.isAssignableFrom(JWTAuthorizationToken.class)) {
			return true;
		}
		return false;
	}

}
