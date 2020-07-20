package com.patho.main.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String LoginFaildSessionAttribue = "loginFailedMessage";

    public void removeLoginFailedMessagedAttribute(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(LoginFaildSessionAttribue);
        }
    }

    /**
     * Refreshes the current Session
     */
    public void keepSessionAlive() {
        logger.debug("Refreshing Session");
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
        request.getSession();
    }

}
