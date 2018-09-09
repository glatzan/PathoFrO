package com.patho.main.config.security.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.patho.main.config.security.LoginHandler;
import com.patho.main.config.security.util.Response;

@Component
public class UserAuthenticationFailureHandler implements AuthenticationFailureHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException e) throws IOException, ServletException {

		HttpSession session = request.getSession(false);
		if (session != null) {
			session.setAttribute(LoginHandler.LoginFaildSessionAttribue, "Einloggen Fehlgeschlagen");
		}
		redirectStrategy.sendRedirect(request, response, "/login.xhtml?error=pw");
	}
}

class MyResponseRequestWrapper extends HttpServletResponseWrapper {
	public MyResponseRequestWrapper(HttpServletResponse response) {
		super(response);
	}
}