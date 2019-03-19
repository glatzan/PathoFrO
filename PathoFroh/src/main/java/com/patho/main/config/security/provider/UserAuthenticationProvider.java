package com.patho.main.config.security.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.patho.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.patho.main.config.security.util.UserAuthorizationToken;
import com.patho.main.model.user.HistoUser;
import com.patho.main.service.AuthenticationService;
import com.patho.main.util.ldap.LDAPAuthenticationException;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private UserService userService;

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UserAuthorizationToken auth = (UserAuthorizationToken) authentication;

		Optional<HistoUser> user;
		try {
			user = authenticationService.authenticate(auth.getPrincipal().toString(), auth.getCredentials().toString());
		} catch (LDAPAuthenticationException e) {
			// authentication was not successful, but user is in database
			throw new BadCredentialsException("Invalid Password or User");
		}

		HistoUser hUser;

		// user is not present in database check if present in ldap
		if (!user.isPresent()) {
			// user exists within the ldap domain, not known to the local program
			if (authenticationService.authenticateWithLDAP(auth.getPrincipal().toString(),
					auth.getCredentials().toString())) {
				// create new user and go to request permission page
				
				Optional<HistoUser> newUSer = userService.createUser(auth.getPrincipal().toString());
				
				if(newUSer.isPresent()) {
					hUser = newUSer.get();
				}else
					throw new BadCredentialsException(
							"Error while loading user data, please contact your administrator");
			} else {
				// user is not known in the ldap domain and in the local program -> throw error
				throw new BadCredentialsException("Invalid Password or User");
			}
		} else {
			hUser = user.get();
		}

		List<GrantedAuthority> t = new ArrayList<GrantedAuthority>();
		t.add(new SimpleGrantedAuthority("USER"));

		return new UsernamePasswordAuthenticationToken(hUser, null, t);
	}

	public boolean supports(Class<?> authentication) {
		if (authentication.isAssignableFrom(UserAuthorizationToken.class)) {
			return true;
		}
		return false;
	}

}
