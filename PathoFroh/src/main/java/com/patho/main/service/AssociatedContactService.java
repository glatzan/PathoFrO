package com.patho.main.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.ContactRole;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.AssociatedContactNotification;
import com.patho.main.model.Person;
import com.patho.main.model.Person_;
import com.patho.main.model.Physician;
import com.patho.main.model.Physician_;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.AssociatedContactNotificationRepository;
import com.patho.main.repository.AssociatedContactRepository;
import com.patho.main.repository.PersonRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.repository.TaskRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class AssociatedContactService extends AbstractService {

	@Autowired
	private PathoConfig pathoConfig;

	@Autowired
	private PhysicianRepository physicianRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private AssociatedContactRepository associatedContactRepository;

	@Autowired
	private AssociatedContactNotificationRepository associatedContactNotificationRepository;

	/**
	 * Loads the predifend notification methods for the specific roles and applies
	 * them on the contact.
	 * 
	 * @param task
	 * @param associatedContact
	 */
	public void updateNotificationsOnRoleChange(Task task, AssociatedContact associatedContact) {

		if (associatedContact.getNotifications() == null) {
			associatedContact.setNotifications(new ArrayList<AssociatedContactNotification>());
		}

		// do nothing if there are some notifications
		if (associatedContact.getNotifications().size() != 0) {
			return;
		}

		List<AssociatedContactNotification.NotificationTyp> types = pathoConfig.getDefaultNotifications()
				.getDefaultNotificationForRole(associatedContact.getRole());

		for (AssociatedContactNotification.NotificationTyp notificationTyp : types) {
			addNotificationType(task, associatedContact, notificationTyp);
		}

		updateNotificationWithDiagnosisPresets(task, associatedContact);
	}

	/**
	 * Checks all diagnoses for physical diagnosis report sending, and checks if the
	 * contact is a affected. If so the contact will be marked.
	 * 
	 * @param task
	 * @param associatedContact
	 */
	public ContactReturn updateNotificationWithDiagnosisPresets(Task task, AssociatedContact associatedContact) {
		Set<ContactRole> sendLetterTo = new HashSet<ContactRole>();

		// checking if a already a report should be send physically, if so do
		// nothing and return
		if (associatedContact.getNotifications().stream()
				.anyMatch(p -> p.getNotificationTyp().equals(AssociatedContactNotification.NotificationTyp.LETTER)))
			return new ContactReturn(task, associatedContact);

		// collecting roles for which a report should be send by letter
		for (DiagnosisRevision diagnosisRevision : task.getDiagnosisRevisions()) {
			for (Diagnosis diagnosis : diagnosisRevision.getDiagnoses()) {
				if (diagnosis.getDiagnosisPrototype() != null)
					sendLetterTo.addAll(diagnosis.getDiagnosisPrototype().getDiagnosisReportAsLetter());
			}
		}

		// checking if contact is within the send letter to roles
		for (ContactRole contactRole : sendLetterTo) {
			if (associatedContact.getRole().equals(contactRole)) {
				// adding notification and return;
				return new ContactReturn(addNotificationByType(task, associatedContact,
						AssociatedContactNotification.NotificationTyp.LETTER).getTask(), associatedContact);
			}
		}

		return new ContactReturn(task, associatedContact);
	}

	/**
	 * Updates all contacts an checks if a physical letter should be send to them
	 * (depending on the selected diagnosis)
	 * 
	 * @param task
	 * @param diagnosisRevision
	 */
	public Task updateNotificationsForPhysicalDiagnosisReport(Task task) {
		for (AssociatedContact associatedContact : task.getContacts()) {
			task = updateNotificationWithDiagnosisPresets(task, associatedContact).getTask();
		}
		return task;
	}

	public void reOrderContactList(Task task, int indexRemove, int indexMove) {
		AssociatedContact remove = task.getContacts().remove(indexRemove);

		task.getContacts().add(indexMove, remove);

		taskRepository.save(task, resourceBundle.get("log.contact.list.reoder"));
	}

	/**
	 * removes a contact
	 * 
	 * @param task
	 * @param associatedContact
	 */
	public void removeAssociatedContact(Task task, AssociatedContact associatedContact) {
		task.getContacts().remove(associatedContact);
		// only associatedContact has to be removed, is the mapping enity
		associatedContactRepository.delete(associatedContact,
				resourceBundle.get("log.contact.remove", associatedContact));
	}

	/**
	 * removes a notification
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notification
	 */
	public Task removeNotification(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification notification) {

		if (associatedContact.getNotifications() != null) {
			associatedContact.getNotifications().remove(notification);

			task = taskRepository.save(task, resourceBundle.get("log.contact.notification.removed", task,
					associatedContact.toString(), notification.getNotificationTyp().toString()));

			// only remove from array, and deleting the entity only (no saving
			// of contact necessary because mapped within notification)
			associatedContactNotificationRepository.delete(notification);
		}

		return task;
	}

	/**
	 * Adds an associated contact, and checks if notification methods should be
	 * added because of a specific diagnosis
	 * 
	 * @param task
	 * @param associatedContact
	 * @return
	 */
	public ContactReturn addAssociatedContactAndUpdateWithDiagnosisPresets(Task task,
			AssociatedContact associatedContact) {

		if (task.getContacts().stream().anyMatch(p -> p.getPerson().getId() == associatedContact.getPerson().getId()))
			throw new IllegalArgumentException("Already in list");

		task.getContacts().add(associatedContact);
		associatedContact.setTask(task);

		task = taskRepository.save(task, resourceBundle.get("log.contact.add", task, associatedContact));
		task = updateNotificationWithDiagnosisPresets(task, associatedContact).getTask();

		return new ContactReturn(task, associatedContact);
	}

	/**
	 * Adds an associated contact
	 * 
	 * @param task
	 * @param associatedContact
	 * @return
	 */
	public ContactReturn addAssociatedContact(Task task, Person person, ContactRole role) {
		return addAssociatedContact(task, new AssociatedContact(task, person, role));
	}

	/**
	 * Adds an associated contact
	 * 
	 * @param task
	 * @param associatedContact
	 * @return
	 */
	public ContactReturn addAssociatedContact(Task task, AssociatedContact associatedContact) {

		if (task.getContacts().stream().anyMatch(p -> p.getPerson().getId() == associatedContact.getPerson().getId()))
			throw new IllegalArgumentException("Already in list");

		task.getContacts().add(associatedContact);
		associatedContact.setTask(task);
		task = taskRepository.save(task, resourceBundle.get("log.contact.add", task, associatedContact));

		return new ContactReturn(task, associatedContact);
	}

	/**
	 * Adds a new notification with the given type
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notificationTyp
	 * @return
	 */
	public NotificationReturn addNotificationByType(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp) {
		return addNotificationByType(task, associatedContact, notificationTyp, true, false, false, null, null, false);

	}

	/**
	 * Adds a new notification with the given type
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notificationTyp
	 * @param active
	 * @param performed
	 * @param failed
	 * @param dateOfAction
	 * @return
	 */
	public NotificationReturn addNotificationByType(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp, boolean active, boolean performed,
			boolean failed, Date dateOfAction, String customAddress, boolean renewed) {

		AssociatedContactNotification newNotification = new AssociatedContactNotification();
		newNotification.setActive(active);
		newNotification.setPerformed(performed);
		newNotification.setDateOfAction(dateOfAction);
		newNotification.setFailed(failed);
		newNotification.setNotificationTyp(notificationTyp);
		newNotification.setContact(associatedContact);
		newNotification.setContactAddress(customAddress);
		newNotification.setRenewed(renewed);

		if (associatedContact.getNotifications() == null)
			associatedContact.setNotifications(new ArrayList<AssociatedContactNotification>());

		associatedContact.getNotifications().add(newNotification);

		task = taskRepository.save(task, resourceBundle.get("log.contact.notification.added", task, associatedContact,
				notificationTyp.toString()));

		return new NotificationReturn(task, newNotification);
	}

	/**
	 * Sets all notifications of the given type to inactive, and creats a new
	 * notification of the given type
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notificationTyp
	 * @return
	 */
	public NotificationReturn addNotificationByTypeAndDisableOld(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp) {
		task = markNotificationsAsActive(task, associatedContact, notificationTyp, false);
		return addNotificationByType(task, associatedContact, notificationTyp);
	}

	/**
	 * Sets the given notification as inactive an adds a new notification of the
	 * same type (active)
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notification
	 */
	public NotificationReturn renewNotification(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification notification) {

		notification.setActive(false);

		task = taskRepository.save(task, resourceBundle.get("log.contact.notification.inactive", task,
				associatedContact, notification.getNotificationTyp().toString(), "inactive"));

		return addNotificationByType(task, associatedContact, notification.getNotificationTyp(), true, false, false,
				null, notification.getContactAddress(), true);
	}

	/**
	 * Sets all notifications with the given type to the given active status
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notificationTyp
	 * @param active
	 */
	public Task markNotificationsAsActive(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp, boolean active) {

		for (AssociatedContactNotification notification : associatedContact.getNotifications()) {
			if (notification.getNotificationTyp().equals(notificationTyp) && notification.isActive()) {
				notification.setActive(active);
				task = taskRepository.save(task,
						resourceBundle.get("log.contact.notification.inactive", task, associatedContact,
								notification.getNotificationTyp().toString(), active ? "active" : "inactive"));
			}
		}

		return task;
	}

	/**
	 * Updates a notification when the notification was performed
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notification
	 * @param message
	 * @param success
	 * @return
	 */
	public Task performNotification(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification notification, String message, boolean success) {
		notification.setActive(false);

		notification.setPerformed(true);
		notification.setDateOfAction(new Date(System.currentTimeMillis()));
		notification.setCommentary(message);
		// if success = performed, nothing to do = inactive, if failed = active
		notification.setActive(!success);
		// if success = !failed = false
		notification.setFailed(!success);

		task = taskRepository.save(task, resourceBundle.get("log.contact.notification.performed", associatedContact,
				notification.getNotificationTyp().toString(), success, message));

		return task;
	}

	/**
	 * Gets the Physician object for a person an returns the associated roles.
	 * 
	 * @param person
	 * @param showOnlyRolesIfAvailable
	 * @return
	 */
	public ContactRole[] getDefaultAssociatedRoleForPhysician(Person person, ContactRole[] showOnlyRolesIfAvailable) {
		Optional<Physician> result = physicianRepository.findById(person.getId());

		if (!result.isPresent())
			return new ContactRole[] { ContactRole.NONE };
		else if (showOnlyRolesIfAvailable == null)
			return result.get().getAssociatedRolesAsArray();
		else {
			ArrayList<ContactRole> resultArr = new ArrayList<ContactRole>();

			for (ContactRole contactRole : result.get().getAssociatedRolesAsArray()) {
				for (ContactRole showOnly : showOnlyRolesIfAvailable) {
					if (contactRole == showOnly) {
						resultArr.add(contactRole);
						break;
					}
				}
			}

			return resultArr.toArray(new ContactRole[resultArr.size()]);
		}
	}

	/**
	 * Increments the count by times the physician was selected.
	 * 
	 * @param person
	 * @return
	 */
	public Physician incrementContactPriorityCounter(Person person) {
		Optional<Physician> p = physicianRepository.findOptionalByPerson(person);

		if (p.isPresent()) {
			p.get().setPriorityCount(p.get().getPriorityCount() + 1);
			return physicianRepository.save(p.get());
		}

		return null;

	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class ContactReturn {
		private Task task;
		private AssociatedContact associatedContact;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class NotificationReturn {
		private Task task;
		private AssociatedContactNotification associatedContactNotification;
	}

}
