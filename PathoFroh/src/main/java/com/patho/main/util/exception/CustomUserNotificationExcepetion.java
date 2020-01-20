package com.patho.main.util.exception;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.service.impl.SpringContextBridge;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
