package com.patho.main.config.security.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patho.main.config.security.util.JWTAuthorizationToken;
import com.patho.main.config.security.util.JWTPasswortApplicationCreds;
import com.patho.main.config.security.util.JWTPasswortApplicationToken;

/**
 * This filter will create a jwt token for a user. It is accessible through
 * /rest/login and takes a json post. Content is: {"username" : "", "password" :
 * ""}
 * 
 * @author andi
 *
 */
public class JWTPasswortAuthorizationFilter extends AbstractAuthenticationProcessingFilter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String tokenPrefix;

	private String headerString;

	public JWTPasswortAuthorizationFilter(AuthenticationManager authenticationManager, ApplicationContext ctx,
			AuthenticationFailureHandler failureHandler) {
		super("/rest/login"); // allow any request to contain an authorization header

		// @value does not work in security filters
		this.tokenPrefix = ctx.getEnvironment().getProperty("patho.jwt.tokenPrefix");
		this.headerString = ctx.getEnvironment().getProperty("patho.jwt.headerString");

		setAuthenticationFailureHandler(failureHandler);
		setAuthenticationManager(authenticationManager);
	}

	/**
	 * Authentificates the user and generates jwt token
	 */
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			// maps the post body to the object
			JWTPasswortApplicationCreds creds = new ObjectMapper().readValue(request.getInputStream(),
					JWTPasswortApplicationCreds.class);
			logger.debug("Checking if user is authenticated");
			// authenticates the users
			return getAuthenticationManager().authenticate(new JWTPasswortApplicationToken(creds));

		} catch (IOException e) {
		}

		logger.debug("User login failed!");
		throw new UsernameNotFoundException("Invalid Password or User");
	}

	/**
	 * Returns the generated token as an http response
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		response.addHeader(headerString, tokenPrefix + " " + ((JWTAuthorizationToken) authResult).getToken());
		response.getWriter()
				.write(headerString + " " + tokenPrefix + " " + ((JWTAuthorizationToken) authResult).getToken());
		response.getWriter().flush();
		response.getWriter().close();
	}
}
