package com.patho.main.config.security.util;

import com.patho.main.model.user.HistoUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Class for holding jwt token
 * @author andi
 *
 */
@Getter
@Setter
public class JWTAuthorizationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 9193997044541649135L;

	private String token;
	
	private HistoUser user;
	
	public JWTAuthorizationToken(String token ) {
		super(null);
		this.token = token;
	}

	public Object getCredentials() {
		return null;
	}

	public Object getPrincipal() {
		return user;
	}

}
