package com.patho.main.util.exception;

import com.patho.main.service.impl.SpringContextBridge;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomUserNotificationExcepetion extends RuntimeException {

    private static final long serialVersionUID = -57860678338686758L;
    private String headline;
    private String message;

    public CustomUserNotificationExcepetion(String headline, String message) {
        super();
        this.message = SpringContextBridge.services().getResourceBundle().get(message);
        this.headline = SpringContextBridge.services().getResourceBundle().get(headline);
    }
}
