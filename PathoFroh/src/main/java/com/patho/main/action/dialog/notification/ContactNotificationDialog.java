package com.patho.main.action.dialog.notification;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.notification.NotificationTyp;
import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.model.patient.notification.ReportIntentNotification;
import com.patho.main.repository.AssociatedContactRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.util.dialog.event.ReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
@Configurable
@Getter
@Setter
public class ContactNotificationDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactRepository associatedContactRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	private ReportIntent associatedContact;

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

	public ContactNotificationDialog initAndPrepareBean(Task task, ReportIntent reportIntent) {
		if (initBean(task, reportIntent))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task, ReportIntent reportIntent) {

		super.initBean(task, Dialog.CONTACTS_NOTIFICATION);

		setAssociatedContact(reportIntent);

		update(false);

		setSelectableRoles(ContactRole.values());

		return true;
	}

	/**
	 * Reloads data of the task
	 */
	public void update(boolean reload) {
		if (reload) {
			setTask(taskRepository.findByID(task.getId(), false, false, false, true, true));
			for (ReportIntent contact : task.getContacts()) {
				if (contact.equals(getAssociatedContact())) {
					setAssociatedContact(contact);
					break;
				}
			}
		}

		generatedMenuModel();
	}

	public void generatedMenuModel() {
		NotificationTyp[] typeArr = { NotificationTyp.EMAIL, NotificationTyp.FAX,
				NotificationTyp.LETTER, NotificationTyp.PHONE };

		model = new DefaultMenuModel();

		for (int i = 0; i < typeArr.length; i++) {
			boolean disabled = false;

			if (getAssociatedContact().getNotifications() != null)
				for (ReportIntentNotification reportIntentNotification : getAssociatedContact()
						.getNotifications()) {
//					if (reportIntentNotification.getNotificationTyp().equals(typeArr[i])
//							&& reportIntentNotification.getActive() && !reportIntentNotification.getFailed()) {
//						disabled = true;
//						break;
//					}
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

	public void removeNotification(ReportIntentNotification reportIntentNotification) {
//		associatedContactService.removeNotification(associatedContact, reportIntentNotification)
//				.getReportIntent();
		update(true);
	}

	public void addNotification(NotificationTyp notification) {
//		associatedContactService.addNotificationByTypeAndDisableOld(associatedContact, notification)
//				.getReportIntent();
		update(true);
	}

	public void notificationAsPerformed(ReportIntentNotification reportIntentNotification) {
//		associatedContactService.performNotification(associatedContact, reportIntentNotification,
//				resourceBundle.get("log.contact.notification.performed.manual"), true).getReportIntent();
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
