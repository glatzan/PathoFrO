package com.patho.main.config.security.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.common.View;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;

@Component
public class UserAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private GlobalEditViewHandler globalEditViewHandler;
	
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		if ((authentication.getPrincipal() instanceof HistoUser)) {

//			SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request,
//					(HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse());

			// getting the users default page
			HistoUser user = (HistoUser) authentication.getPrincipal();

			// always one group
			HistoGroup group = user.getAuthorities().get(0);

//			if (savedRequest != null) {
//				try {
//
//					for (View view : user.getSettings().getAvailableViews()) {
//
//					}
//					// TODO check if path is allowed!, reanable redirect
//
//					// worklistViewHandlerAction.initBean();
//					// URL url = new URL(savedRequest.getRedirectUrl());
//					// return url.getFile().substring(request.getContextPath().length());
//				} catch (Exception e) {
//					logger.error(e.getMessage() + " Using default URL");
//				}
//			}

			if (user.getSettings().getStartView() != null) {
				// alway init worklist!

				if (user.getSettings().getStartView() != View.GUEST)
					globalEditViewHandler.initializeDataForSession();
				redirectStrategy.sendRedirect(request, response, user.getSettings().getStartView().getRootPath());
			} else {
				logger.error("No Start View Found, going back to normal view");
				redirectStrategy.sendRedirect(request, response, View.LOGIN.getPath());
			}
		}else
			redirectStrategy.sendRedirect(request, response, View.LOGIN.getPath());
	}
}
