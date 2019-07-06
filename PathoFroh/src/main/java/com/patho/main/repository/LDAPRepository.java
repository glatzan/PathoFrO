package com.patho.main.repository;

import com.patho.main.model.Physician;

import java.util.List;
import java.util.Optional;

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