package com.patho.main.repository;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ldap.query.ContainerCriteria;

import com.patho.main.model.Physician;
import com.patho.main.model.dto.ldap.LDAPUserMapper;

public interface LDAPRepository {

	/**
	 * Returns an user with the given uid. If not found an empty Optional will be
	 * returned.
	 * 
	 * @param name
	 * @return
	 */
	public Optional<Physician> findByUid(String name);

	/**
	 * Returns a list with physicians matching the given parameters. It is seached
	 * for cn (contains name, surname and title).
	 * 
	 * @param name
	 * @return
	 */
	public List<Physician> findAllByName(String... name);

	/**
	 * Returns a list with physicians matching the given parameters. It is seached
	 * for cn (contains name, surname and title).
	 * 
	 * @param names
	 * @return
	 */
	public List<Physician> findAllByName(List<String> names);
}