package com.patho.main.config.security.filters;

import com.patho.main.config.security.util.JWTAuthorizationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTAuthorizationFilter extends AbstractAuthenticationProcessingFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String tokenPrefix;

    private String headerString;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, ApplicationContext ctx, AuthenticationFailureHandler failureHandler) {
        super("/rest/*"); // allow any request to contain an authorization header

        // @value does not work in security filters
        this.tokenPrefix = ctx.getEnvironment().getProperty("patho.jwt.tokenPrefix");
        this.headerString = ctx.getEnvironment().getProperty("patho.jwt.headerString");

        setAuthenticationFailureHandler(failureHandler);
        setAuthenticationManager(authenticationManager);

    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String header = request.getHeader(headerString);

        if (header == null || !header.startsWith(tokenPrefix)) {
            logger.debug("No auth header found!");
            throw new BadCredentialsException("Invalid Token");
        }

        JWTAuthorizationToken token = new JWTAuthorizationToken(header);

        SecurityContextHolder.getContext().setAuthentication(token);

        logger.debug("Authheader found, continue...");

        return getAuthenticationManager().authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }

}
