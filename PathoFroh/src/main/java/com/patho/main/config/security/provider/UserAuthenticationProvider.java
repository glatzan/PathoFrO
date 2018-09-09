package com.patho.main.config.security.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UserAuthorizationToken auth = (UserAuthorizationToken) authentication;

		Optional<HistoUser> user;
		try {
			user = authenticationService.authenticate(auth.getPrincipal().toString(), auth.getCredentials().toString());
		} catch (LDAPAuthenticationException e) {
			// authentication was not successful, but user is in database
			throw new BadCredentialsException("Invalid Password or User");
		}

		// user is not present in database check if present in ldap
		if (!user.isPresent()) {
			// user exists within the ldap domain, not known to the local program
			if (authenticationService.authenticateWithLDAP(auth.getPrincipal().toString(),
					auth.getCredentials().toString())) {
				// create new user and go to request permission page

			} else {
				// user is not known in the ldap domain and in the local program -> throw error
				throw new BadCredentialsException("Invalid Password or User");
			}
		}

		List<GrantedAuthority> t = new ArrayList<GrantedAuthority>();
		t.add(new SimpleGrantedAuthority("USER"));

		return new UsernamePasswordAuthenticationToken(user.get(), null, t);
	}

	public boolean supports(Class<?> authentication) {
		if (authentication.isAssignableFrom(UserAuthorizationToken.class)) {
			return true;
		}
		return false;
	}
	
	
}

/*
 * if (globalSettings.getProgramSettings().isOffline()) {
 * logger.info("LDAP login disabled");
 * 
 * HistoUser histoUser = transientDAO.loadUserByName(userName); if (histoUser ==
 * null) { logger.info("No user found, creating new one"); histoUser = new
 * HistoUser(userName);
 * 
 * HistoGroup group = transientDAO.getHistoGroup(HistoGroup.GROUP_GUEST_ID,
 * true); histoUser.setGroup(group); histoUser.setSettings(new HistoSettings());
 * // copy settings from group to user
 * CopySettingsUtil.copyCrucialGroupSettings(histoUser, group, true);
 * 
 * } else if (histoUser.getPhysician() == null) { histoUser.setPhysician(new
 * Physician(new Person(new Contact())));
 * histoUser.getPhysician().setUid(userName); }
 * 
 * // throwing error if person is banned if(histoUser.getGroup().getAuthRole()
 * == AuthRole.ROLE_NONEAUTH || histoUser.isArchived()) { throw new
 * BadCredentialsException(resourceBundle.get("login.error.banned")); }
 * 
 * histoUser.setLastLogin(System.currentTimeMillis());
 * 
 * transientDAO.save(histoUser, "log.user.update", new Object[] {
 * histoUser.toString() });
 * 
 * Collection<? extends GrantedAuthority> authorities =
 * histoUser.getAuthorities();
 * 
 * return new UsernamePasswordAuthenticationToken(histoUser, password,
 * authorities); }
 * 
 * LdapHandler ladpHandler = globalSettings.getLdapHandler(); DirContext
 * connection = ladpHandler.openConnection();
 * 
 * Physician physician = ladpHandler.getPhyscican(connection, userName);
 * 
 * if (physician != null) { String dn = physician.getDnObjectName() + "," + base
 * + "," + suffix;
 * 
 * ladpHandler.closeConnection(connection);
 * 
 * logger.info("Physician found " + physician.getPerson().getFullName());
 * 
 * // if no error was thrown auth was successful //ladpHandler.checkPassword(dn,
 * password);
 * 
 * logger.info("Login successful " + physician.getPerson().getFullName());
 * 
 * // checking if histouser exsists HistoUser histoUser =
 * transientDAO.loadUserByName(userName);
 * 
 * if (histoUser == null) { logger.info("Creating new HistoUser " +
 * physician.getPerson().getFullName()); histoUser = new HistoUser(userName);
 * 
 * HistoGroup group = transientDAO.getHistoGroup(HistoGroup.GROUP_GUEST_ID,
 * true); histoUser.setGroup(group); histoUser.setSettings(new HistoSettings());
 * CopySettingsUtil.copyCrucialGroupSettings(histoUser, group, true);
 * 
 * // if the physician was added as surgeon the useracc an the // physician will
 * be merged Physician physicianFromDatabase =
 * transientDAO.loadPhysicianByUID(userName); if (physicianFromDatabase != null)
 * { histoUser.setPhysician(physicianFromDatabase);
 * logger.info("Physician already in datanse " +
 * physician.getPerson().getFullName()); } else { // creating new physician an
 * person histoUser.setPhysician(new Physician(new Person(new Contact())));
 * histoUser.getPhysician().setUid(userName); // Default role for that physician
 * histoUser.getPhysician().addAssociateRole(ContactRole.OTHER_PHYSICIAN); } }
 * 
 * // throwing error if person is banned if(histoUser.getGroup().getAuthRole()
 * == AuthRole.ROLE_NONEAUTH || histoUser.isArchived()) { throw new
 * BadCredentialsException(resourceBundle.get("login.error.banned")); }
 * 
 * histoUser.setLastLogin(System.currentTimeMillis());
 * 
 * transientDAO.mergePhysicians(physician , histoUser.getPhysician());
 * 
 * transientDAO.save(histoUser, "log.user.update", new Object[] {
 * histoUser.toString() });
 * 
 * Collection<? extends GrantedAuthority> authorities =
 * histoUser.getAuthorities();
 * 
 * return new UsernamePasswordAuthenticationToken(histoUser, password,
 * authorities); } else throw new
 * BadCredentialsException(resourceBundle.get("login.error.text")); } catch
 * (NamingException | IOException | AuthenticationException e) { // catch other
 * exception an merge them into a bad credentials exception if(e instanceof
 * BadCredentialsException) throw (BadCredentialsException)e;
 * 
 * throw new BadCredentialsException(resourceBundle.get("login.error.text")); }
 */