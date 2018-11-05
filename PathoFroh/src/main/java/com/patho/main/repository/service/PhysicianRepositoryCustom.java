package com.patho.main.repository.service;

import java.util.List;
import java.util.Optional;

import com.patho.main.common.ContactRole;
import com.patho.main.common.SortOrder;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Task;
import com.patho.main.ui.selectors.PhysicianSelector;

public interface PhysicianRepositoryCustom {

	/**
	 * Loads a physician by id, if loadOrganizations is true, the orgnaization of
	 * the associated person are loaded as well
	 * 
	 * @param id
	 * @param loadOrganizations
	 * @return
	 */
	public Optional<Physician> findOptionalByID(long id, boolean loadOrganizations);

	/**
	 * Returns a list of all physicians which are associated with the given role.
	 * 
	 * @param role
	 * @param irgnoreArchived
	 * @return
	 */
	List<Physician> findAllByRole(ContactRole role, boolean irgnoreArchived);

	/**
	 * Returns a list of all physicians which are associated with at least one given
	 * role.
	 * 
	 * @param roles
	 * @param irgnoreArchived
	 * @return
	 */
	List<Physician> findAllByRole(List<ContactRole> roles, boolean irgnoreArchived);

	/**
	 * Returns a list of all physicians which are associated with at least one given
	 * role.
	 * 
	 * @param roles
	 * @param irgnoreArchived
	 * @return
	 */
	List<Physician> findAllByRole(ContactRole[] roles, boolean irgnoreArchived);

	/**
	 * Returns a list of all physicians which are associated with at least one given
	 * role.
	 * 
	 * @param roles
	 * @param irgnoreArchived
	 * @param sortOrder
	 * @return
	 */
	List<Physician> findAllByRole(ContactRole[] roles, boolean irgnoreArchived, SortOrder sortOrder);

	/**
	 * Returns a list of all physicians which are associated with at least one given
	 * role.
	 * 
	 * @param roles
	 * @param irgnoreArchived
	 * @param sortOrder
	 * @return
	 */
	List<Physician> findAllByRole(List<ContactRole> roles, boolean irgnoreArchived, SortOrder sortOrder);

	List<PhysicianSelector> findSelectorsByRole(Task task, ContactRole roles, SortOrder sortOrder);

	List<PhysicianSelector> findSelectorsByRole(Task task, ContactRole[] roles, SortOrder sortOrder);

	List<PhysicianSelector> findSelectorsByRole(Task task, List<ContactRole> roles, SortOrder sortOrder);
}
