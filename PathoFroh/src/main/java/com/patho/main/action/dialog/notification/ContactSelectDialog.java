package com.patho.main.action.dialog.notification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.common.SortOrder;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.ui.selectors.PhysicianSelector;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
@Slf4j
public class ContactSelectDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianRepository physicianRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactService associatedContactService;
	
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	/**
	 * List of all ContactRole available for selecting physicians, used by contacts
	 * and settings
	 */
	private ContactRole[] selectAbleContactRoles;

	/**
	 * Array of roles for that physicians should be shown.
	 */
	private ContactRole[] showRoles;

	private ContactRole[] addableRoles;

	/**
	 * Role of the quick associatedContact select dialog, either SURGEON or
	 * PRIVATE_PHYSICIAN
	 */
	private ContactRole addAsRole;

	/**
	 * List contain contacts to select from, used by contacts
	 */
	private List<PhysicianSelector> contactList;

	/**
	 * For quickContact selection
	 */
	private PhysicianSelector selectedContact;

	/**
	 * If true the user can change the role, for the the physician is added
	 */
	private boolean manuallySelectRole = false;

	/**
	 * IF true the role to add an physician will be determined by the physicians
	 * roles matching the addableRoles. If there are several matching roles the
	 * first matching role will be used.
	 */
	private boolean dynamicRoleSelection = false;

	public void initAndPrepareBean(Task task, ContactRole... contactRole) {
		if (initBean(task, contactRole, contactRole, contactRole, contactRole.length == 1 ? contactRole[0] : null))
			prepareDialog();
	}

	public void initAndPrepareBean(Task task, ContactRole[] selectAbleRoles, ContactRole[] showRoles,
			ContactRole addAsRole) {
		if (initBean(task, selectAbleRoles, showRoles, new ContactRole[] { addAsRole }, addAsRole))
			prepareDialog();
	}

	public void initAndPrepareBean(Task task, ContactRole[] selectAbleRoles, ContactRole[] showRoles,
			ContactRole[] addableRoles, ContactRole addAsRole) {
		if (initBean(task, selectAbleRoles, showRoles, addableRoles, addAsRole))
			prepareDialog();
	}

	public boolean initBean(Task task, ContactRole[] selectAbleRoles, ContactRole[] showRoles,
			ContactRole[] addableRoles, ContactRole addAsRole) {

		super.initBean(task, Dialog.QUICK_CONTACTS);

		setSelectAbleContactRoles(selectAbleRoles);

		setShowRoles(showRoles);

		setAddAsRole(addAsRole);

		// if no role to add, the dynamic role selection
		if (getAddAsRole() == null)
			setDynamicRoleSelection(true);

		setAddableRoles(addableRoles);

		setSelectedContact(null);

		setManuallySelectRole(false);

		updateContactList();

		return true;
	}

	/**
	 * updates the associatedContact list if selection of contacts was changed (more
	 * or other roles should be displayed)
	 */
	public void updateContactList() {
		List<Physician> databasePhysicians = physicianRepository.findByRole(getShowRoles(), true, SortOrder.PRIORITY);
		setContactList(PhysicianSelector.factory(task, databasePhysicians));
	}

	public void addPhysicianAsRole() {
		if (getSelectedContact() != null) {

			AssociatedContact associatedContact = new AssociatedContact(getTask(),
					getSelectedContact().getPhysician().getPerson());
			addPhysicianAsRole(associatedContact, getRoleForAddingContact(getSelectedContact().getPhysician()));
		}
	}

	/**
	 * IF dynamicRoleSelection is disabled the addAsRole will be used for adding a
	 * physician. If true the addableRoles an the roles of the physician will be
	 * compared and the first match will be used;
	 * 
	 * @param physician
	 * @return
	 */
	private ContactRole getRoleForAddingContact(Physician physician) {
		if (dynamicRoleSelection) {
			for (ContactRole contactRole : addableRoles) {
				for (ContactRole physicianRole : physician.getAssociatedRoles()) {
					if (contactRole == physicianRole)
						return physicianRole;
				}
			}
		} else
			getAddAsRole();

		return ContactRole.OTHER_PHYSICIAN;
	}

	/**
	 * Sets the given associatedContact to the given role
	 * 
	 * @param associatedContact
	 * @param role
	 */
	public void addPhysicianAsRole(AssociatedContact associatedContact, ContactRole role) {
		try {
			associatedContact.setRole(role);

			// saving
			associatedContactService.addAssociatedContact(task, associatedContact);

			// settings roles
			associatedContactService.updateNotificationForPhysicalDiagnosisReport(task, associatedContact);

			// increment counter
			associatedContactService.incrementContactPriorityCounter(associatedContact.getPerson());
		} catch (IllegalArgumentException e) {
			// todo error message
			log.debug("Not adding, double contact");
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

}
