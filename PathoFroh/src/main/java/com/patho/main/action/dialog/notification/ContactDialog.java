package com.patho.main.action.dialog.notification;

import java.util.List;

import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.ui.selectors.AssociatedContactSelector;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Configurable
@Getter
@Setter
public class ContactDialog extends AbstractDialog<ContactDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactService associatedContactService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private DialogHandler dialogHandler;

	private AssociatedContactSelector[] contacts;

	/**
	 * List of all ContactRole available for selecting physicians, used by contacts
	 * and settings
	 */
	private ContactRole[] selectAbleContactRoles;

	/**
	 * Array of roles for that physicians should be shown.
	 */
	private ContactRole[] showRoles;

	public ContactDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task) {
		super.initBean(task, Dialog.CONTACTS);

		setSelectAbleContactRoles(ContactRole.values());

		setShowRoles(ContactRole.values());

		update(false);

		return true;
	}

	/**
	 * Reloads data of the task
	 */
	public void update(boolean reload) {
		if (reload)
			taskRepository.findOptionalByIdAndInitialize(task.getId(), false, false, false, true, true);

		updateContactHolders();
	}

	public void addContact() {
		System.out.println(dialogHandler);
		dialogHandler.getContactSelectDialog().initAndPrepareBean(task, ContactRole.values(), ContactRole.values(),
				ContactRole.values(), ContactRole.OTHER_PHYSICIAN).setManuallySelectRole(true);
	}

	public void updateContactHolders() {
		if (task.getContacts() != null) {
			List<AssociatedContactSelector> selectors = AssociatedContactSelector.factory(task);
			setContacts(selectors.toArray(new AssociatedContactSelector[selectors.size()]));
		}
	}

	public void removeContact(Task task, AssociatedContact associatedContact) {
		associatedContactService.removeAssociatedContact(task, associatedContact);
	}

	/**
	 * Is fired if the list is reordered by the user via drag and drop
	 *
	 * @param event
	 */
	public void onReorderList(ReorderEvent event) {
		logger.debug("List order changed, moved contact from ? to ?", event.getFromIndex(), event.getToIndex());
		associatedContactService.reOrderContactList(task, event.getFromIndex(), event.getToIndex());
	}

	/**
	 * On dialog return, reload data
	 * 
	 * @param event
	 */
	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
			update(true);
		}
	}

}
