package com.patho.main.action.dialog.notification;

import java.util.List;

import org.primefaces.event.ReorderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.DialogHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.ui.selectors.AssociatedContactSelector;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
@Slf4j
public class ContactDialog extends AbstractDialog {


	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DialogHandlerAction dialogHandlerAction;
	
	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactService associatedContactService;

	private AssociatedContactSelector[] contacts;

	/**
	 * List of all ContactRole available for selecting physicians, used by
	 * contacts and settings
	 */
	private ContactRole[] selectAbleContactRoles;

	/**
	 * Array of roles for that physicians should be shown.
	 */
	private ContactRole[] showRoles;

	public void initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
	}

	public boolean initBean(Task task) {
		super.initBean(task, Dialog.CONTACTS);

		setSelectAbleContactRoles(ContactRole.values());

		setShowRoles(ContactRole.values());

		updateContactHolders();

		return true;
	}

	public void addContact() {
		dialogHandlerAction.getContactSelectDialog().initBean(task, ContactRole.values(), ContactRole.values(),
				ContactRole.values(), ContactRole.OTHER_PHYSICIAN);
		// user can manually change the role for that the physician is added
		dialogHandlerAction.getContactSelectDialog().setManuallySelectRole(true);
		dialogHandlerAction.getContactSelectDialog().prepareDialog();
	}

	public void updateContactHolders() {
		if (task.getContacts() != null) {
			List<AssociatedContactSelector> selectors = AssociatedContactSelector.factory(task);
			setContacts(selectors.toArray(new AssociatedContactSelector[selectors.size()]));
		}
	}

	public void removeContact(Task task, AssociatedContact associatedContact) {
		try {
			associatedContactService.removeAssociatedContact(task, associatedContact);
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	/**
	 * Is fired if the list is reordered by the user via drag and drop
	 *
	 * @param event
	 */
	public void onReorderList(ReorderEvent event) {
		try {

			log.debug(
					"List order changed, moved contact from " + event.getFromIndex() + " to " + event.getToIndex());

			associatedContactService.reOrderContactList(task, event.getFromIndex(), event.getToIndex());

		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

}
