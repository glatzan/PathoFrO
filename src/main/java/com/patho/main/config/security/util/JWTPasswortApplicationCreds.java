package com.patho.main.config.security.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JWTPasswortApplicationCreds {
    private String username;
    private String password;
}