package com.patho.main.config.security.filters;

import com.patho.main.config.security.util.UserAuthorizationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserAuthorizationFilter extends AbstractAuthenticationProcessingFilter {

    public UserAuthorizationFilter(AuthenticationManager authenticationManager,
                                   AuthenticationFailureHandler failureHandler, AuthenticationSuccessHandler successHandler) {
        super("/perform_login");

        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
        setAuthenticationManager(authenticationManager);
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (request.getParameter("username") == null || request.getParameter("password") == null) {
            return null;
        }

        // return a new authentication token to be processed by the authentication
        // provider
        return getAuthenticationManager().authenticate(new UserAuthorizationToken(request.getParameter("username"),
                request.getParameter("password"), request.getParameter("otp")));
    }

}
