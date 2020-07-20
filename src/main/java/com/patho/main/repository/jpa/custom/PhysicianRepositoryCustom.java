package com.patho.main.repository.jpa.custom;

import com.patho.main.common.ContactRole;
import com.patho.main.common.SortOrder;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Task;
import com.patho.main.ui.selectors.PhysicianSelector;

import java.util.List;

public interface PhysicianRepositoryCustom {

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
