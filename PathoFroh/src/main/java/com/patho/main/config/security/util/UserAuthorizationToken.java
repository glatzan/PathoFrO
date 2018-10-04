package com.patho.main.config.security.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import lombok.Getter;
import lombok.Setter;

/**
 * Class for storing user login password and name
 * 
 * @author andi
 *
 */
@Getter
@Setter
public class UserAuthorizationToken extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = 1213800806796822321L;

	private String otp;

	public UserAuthorizationToken(String username, String password, String otp) {
		super(username, password);
		this.otp = otp;
	}
	
}
