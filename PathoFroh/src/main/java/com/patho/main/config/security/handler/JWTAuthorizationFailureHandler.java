package com.patho.main.config.security.handler;

import com.patho.main.config.security.util.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JWTAuthorizationFailureHandler implements AuthenticationFailureHandler {

    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        AuthenticationException e) throws IOException, ServletException {

        String errorMessage = ExceptionUtils.getMessage(e);

        System.out.println("hallo --- ss");
        sendError(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED, errorMessage, e);
    }

    private void sendError(HttpServletResponse response, int code, String message, Exception e) throws IOException {
        SecurityContextHolder.clearContext();

        Response<String> exceptionResponse = new Response<String>(Response.STATUES_FAILURE, message,
                ExceptionUtils.getStackTrace(e));

        exceptionResponse.send(response, code);
    }
}