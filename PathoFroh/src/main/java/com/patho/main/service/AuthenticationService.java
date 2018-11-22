package com.patho.main.service;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.config.security.util.JWTAuthorizationToken;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.UserRepository;
import com.patho.main.util.ldap.LDAPAuthenticationException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Transactional
public class AuthenticationService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LdapTemplate ldapTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Returns the username from a jwt token
	 * 
	 * @param token
	 * @param secret
	 * @param prefix
	 * @return
	 */
	public static String verifyJWTToken(JWTAuthorizationToken token, String secret, String prefix) {
		try {
			return Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token.getToken().replace(prefix, ""))
					.getBody().getSubject();
		} catch (MalformedJwtException e) {
			return null;
		}
	}

	/**
	 * Generates a jwt token for a histoUser
	 * 
	 * @param user
	 * @param secret
	 * @return
	 */
	public static String generateJWTToken(HistoUser user, String secret) {
		String token = Jwts.builder().setSubject(user.getUsername()).claim("issueDate", System.currentTimeMillis())
				.signWith(SignatureAlgorithm.HS512, secret.getBytes()).compact();
		return token;
	}

	/**
	 * Authenticates a user with the program. If it is a local user, the password
	 * will be check, if not a ldap authentication will be performed.
	 * 
	 * @param uuid
	 * @param password
	 * @return Empty Optional if no user was found or login was incorrect.
	 * @throws LDAPAuthenticationException
	 */
	public Optional<HistoUser> authenticate(String uid, String password) throws LDAPAuthenticationException {
		Optional<HistoUser> user = userRepository.findOptionalByPhysicianUid(uid);

		if (user.isPresent()) {
			if (user.get().isLocalUser()) {
				logger.debug("Local user, authentication against local database");
				if (passwordEncoder.matches(password, user.get().getPassword())) {
					return user;
				}
			} else {
				logger.debug("Pdv user, authentication against ldap");
				// throws error if authentication was not successful
				if (authenticateWithLDAP(uid, password))
					return user;
				else
					throw new LDAPAuthenticationException();
			}
		}

		return Optional.empty();
	}

	/**
	 * Authenticates the user with the backend ldap
	 * 
	 * @param uuid
	 * @param password
	 * @return
	 */
	public boolean authenticateWithLDAP(String uuid, String password) {
		try {
			logger.debug("Authenticating with ldap user: " + uuid);
			ldapTemplate.authenticate(query().where("uid").is(uuid), password);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
