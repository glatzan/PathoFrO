package com.patho.main.repository.impl;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.naming.Name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import com.patho.main.model.Physician;
import com.patho.main.model.dto.ldap.LDAPUserMapper;
import com.patho.main.repository.LDAPRepository;

import lombok.Getter;
import lombok.Setter;

@Service
@ConfigurationProperties(prefix = "patho.ldap")
@Getter
@Setter
public class LDAPRepositoryImpl implements LDAPRepository {

	@Autowired
	private LdapTemplate ldapTemplate;

	private String userParentDn;

	private String userDn;

	/**
	 * Returns an user with the given uid. If not found an empty Optional will be
	 * returned.
	 * 
	 * @param name
	 * @return
	 */
	@Override
	public Optional<Physician> findByUid(String name) {
		try {
			LDAPUserMapper userMapper = ldapTemplate.findByDn(buildUserDn(name), LDAPUserMapper.class);
			return Optional.ofNullable(userMapper.getPhysician());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	/**
	 * Returns a list with physicians matching the given parameters. It is seached
	 * for cn (contains name, surname and title).
	 * 
	 * @param name
	 * @return
	 */
	@Override
	public List<Physician> findAllByName(String... name) {
		return findAllByName(Arrays.asList(name));
	}

	/**
	 * Returns a list with physicians matching the given parameters. It is seached
	 * for cn (contains name, surname and title).
	 * 
	 * @param name
	 * @return
	 */
	@Override
	public List<Physician> findAllByName(List<String> names) {
		ContainerCriteria query = query().where("objectclass").is("person");
		names.forEach(p -> query.and(query().where("cn").like("*"+p+"*")));
		List<LDAPUserMapper> result = ldapTemplate.find(query, LDAPUserMapper.class);
		return result.stream().map(p -> p.getPhysician()).collect(Collectors.toList());
	}

	/**
	 * Builds dn for a person
	 * 
	 * @param person
	 * @return
	 */
	protected Name buildUserDn(String person) {
		return buildDn(userParentDn, userDn, person);
	}

	/**
	 * Builds dn
	 * 
	 * @param ou
	 * @param dn
	 * @param dnValue
	 * @return
	 */
	protected Name buildDn(String ou, String dn, String dnValue) {
		return LdapNameBuilder.newInstance().add("ou", ou).add(dn, dnValue).build();
	}
}
