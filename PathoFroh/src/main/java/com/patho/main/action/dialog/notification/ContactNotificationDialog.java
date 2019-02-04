package com.patho.main.action.dialog.notification;

import java.util.HashMap;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.AssociatedContactNotification;
import com.patho.main.model.AssociatedContactNotification.NotificationTyp;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.AssociatedContactRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class ContactNotificationDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactService associatedContactService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactRepository associatedContactRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	private AssociatedContact associatedContact;

	private MenuModel model;

	private ContactRole[] selectableRoles;

	private HashMap<String, String> icons = new HashMap<String, String>() {

		private static final long serialVersionUID = 6498119466449178573L;

		{
			put("EMAIL", "fa-envelope");
			put("FAX", "fa-fax");
			put("PHONE", "fa-phone");
			put("LETTER", "fa-pencil-square-o");
		}
	};

	public ContactNotificationDialog initAndPrepareBean(Task task, AssociatedContact associatedContact) {
		if (initBean(task, associatedContact))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task, AssociatedContact associatedContact) {

		super.initBean(task, Dialog.CONTACTS_NOTIFICATION);

		setAssociatedContact(associatedContact);

		update(false);

		setSelectableRoles(ContactRole.values());

		return true;
	}

	/**
	 * Reloads data of the task
	 */
	public void update(boolean reload) {
		if (reload) {
			setTask(taskRepository.findOptionalByIdAndInitialize(task.getId(), false, false, false, true, true).get());
			for (AssociatedContact contact : task.getContacts()) {
				if (contact.equals(getAssociatedContact())) {
					setAssociatedContact(contact);
					break;
				}
			}
		}

		generatedMenuModel();
	}

	public void generatedMenuModel() {
		AssociatedContactNotification.NotificationTyp[] typeArr = { NotificationTyp.EMAIL, NotificationTyp.FAX,
				NotificationTyp.LETTER, NotificationTyp.PHONE };

		model = new DefaultMenuModel();

		for (int i = 0; i < typeArr.length; i++) {
			boolean disabled = false;

			if (getAssociatedContact().getNotifications() != null)
				for (AssociatedContactNotification associatedContactNotification : getAssociatedContact()
						.getNotifications()) {
					if (associatedContactNotification.getNotificationTyp().equals(typeArr[i])
							&& associatedContactNotification.getActive() && !associatedContactNotification.getFailed()) {
						disabled = true;
						break;
					}
				}

			DefaultMenuItem item = new DefaultMenuItem("");
			item.setIcon("fa " + icons.get(typeArr[i].toString()));
			item.setCommand("#{dialog.contactNotificationDialog.addNotification('" + typeArr[i].toString() + "')}");
			item.setValue(resourceBundle.get("enum.notificationType." + typeArr[i].toString()));
			item.setDisabled(disabled);
			item.setUpdate("@form");
			model.addElement(item);
		}
	}

	public void removeNotification(AssociatedContactNotification associatedContactNotification) {
		associatedContactService.removeNotification(associatedContact, associatedContactNotification)
				.getAssociatedContact();
		update(true);
	}

	public void addNotification(AssociatedContactNotification.NotificationTyp notification) {
		associatedContactService.addNotificationByTypeAndDisableOld(associatedContact, notification)
				.getAssociatedContact();
		update(true);
	}

	public void notificationAsPerformed(AssociatedContactNotification associatedContactNotification) {
		associatedContactService.performNotification(associatedContact, associatedContactNotification,
				resourceBundle.get("log.contact.notification.performed.manual"), true).getAssociatedContact();
		update(true);
	}

	public void onRoleChange() {
		associatedContactRepository.save(getAssociatedContact(),
				resourceBundle.get("log.contact.roleChange", getAssociatedContact().getTask(),
						getAssociatedContact().toString(), getAssociatedContact().getRole().toString()));
		update(true);
	}

	@Override
	public void hideDialog() {
		super.hideDialog(new ReloadEvent());
	}

}
