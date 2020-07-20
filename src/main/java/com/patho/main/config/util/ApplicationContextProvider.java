package com.patho.main.config.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    public static ApplicationContext getContext() {

        if (context != null) {
            return context;
        } else {
            throw new IllegalStateException("The Spring application context is not yet available.");
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        if (context == null) {
            ApplicationContextProvider.context = applicationContext;
        } else {
            throw new IllegalStateException("The application context provider was already initialized.");
        }

    }
}