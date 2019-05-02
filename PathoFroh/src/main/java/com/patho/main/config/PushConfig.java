package com.patho.main.config;

import org.primefaces.push.PushServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class PushConfig {

    @Bean
    public ServletRegistrationBean pushServletRegistration() {
        ServletRegistrationBean pushServlet = new ServletRegistrationBean(new PushServlet(), "/primepush/*");
        pushServlet.addInitParameter("org.atmosphere.annotation.packages", "org.primefaces.push");
        pushServlet.addInitParameter("org.atmosphere.cpr.packages", "com.patho.main.config.push");
        pushServlet.setAsyncSupported(true);
        pushServlet.setLoadOnStartup(0);
        pushServlet.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return pushServlet;
    }
}
