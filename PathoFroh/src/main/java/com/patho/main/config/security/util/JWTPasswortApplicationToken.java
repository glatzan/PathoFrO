package com.patho.main.config.security.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@Setter
public class JWTPasswortApplicationToken extends UsernamePasswordAuthenticationToken {

    /**
     *
     */
    private static final long serialVersionUID = -7279566587737255850L;

    public JWTPasswortApplicationToken(JWTPasswortApplicationCreds creds) {
        super(creds.getUsername(), creds.getPassword());
    }

    public JWTPasswortApplicationToken(String username, String password) {
        super(username, password);
    }

}