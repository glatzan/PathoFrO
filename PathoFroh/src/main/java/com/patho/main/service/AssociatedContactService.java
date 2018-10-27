package com.patho.main.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.ContactRole;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.AssociatedContactNotification;
import com.patho.main.model.AssociatedContactNotification.NotificationTyp;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.AssociatedContactNotificationRepository;
import com.patho.main.repository.AssociatedContactRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.util.helper.StreamUtils;
import com.patho.main.util.notification.MailContainer;
import com.patho.main.util.notification.NotificationContainer;

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
	public ContactReturn updateNotificationsOnRoleChange(Task task, AssociatedContact associatedContact) {
		logger.debug("Updating Notifications");

		// do nothing if there are some notifications
		if (associatedContact.getNotifications() != null && associatedContact.getNotifications().size() != 0) {
			return new ContactReturn(associatedContact.getTask(), associatedContact);
		}

		List<AssociatedContactNotification.NotificationTyp> types = pathoConfig.getDefaultNotification()
				.getDefaultNotificationForRole(associatedContact.getRole());

		for (AssociatedContactNotification.NotificationTyp notificationTyp : types) {
			associatedContact = addNotificationByType(associatedContact, notificationTyp).getAssociatedContact();
		}

		return updateNotificationWithDiagnosisPresets(associatedContact.getTask(), associatedContact);
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
		if (associatedContact.getNotifications() != null && associatedContact.getNotifications().stream()
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
				return new ContactReturn(
						addNotificationByType(associatedContact, AssociatedContactNotification.NotificationTyp.LETTER)
								.getTask(),
						associatedContact);
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
				resourceBundle.get("log.contact.remove", task, associatedContact),
				associatedContact.getTask().getPatient());
	}

	/**
	 * removes a notification
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notification
	 */
	public ContactReturn removeNotification(AssociatedContact associatedContact,
			AssociatedContactNotification notification) {

		if (associatedContact.getNotifications() != null) {
			associatedContact.getNotifications().remove(notification);

			associatedContact = associatedContactRepository.save(associatedContact,
					resourceBundle.get("log.contact.notification.removed", associatedContact.getTask(),
							associatedContact.toString(), notification.getNotificationTyp().toString()),
					associatedContact.getTask().getPatient());

			// only remove from array, and deleting the entity only (no saving
			// of contact necessary because mapped within notification)
			associatedContactNotificationRepository.delete(notification);
		}

		return new ContactReturn(associatedContact.getTask(), associatedContact);
	}

	/**
	 * Adds an associated contact, and checks if notification methods should be
	 * added because of a specific diagnosis
	 * 
	 * @param task
	 * @param associatedContact
	 * @return
	 */
	public ContactReturn addAssociatedContactAndAddDefaultNotifications(Task task,
			AssociatedContact associatedContact) {

		for (AssociatedContact contact : task.getContacts()) {
			if (contact.getPerson().equals(associatedContact.getPerson()))
				throw new IllegalArgumentException("Already in list");
		}

		task.getContacts().add(associatedContact);
		associatedContact.setTask(task);

		associatedContact = associatedContactRepository.save(associatedContact,
				resourceBundle.get("log.contact.add", task, associatedContact),
				associatedContact.getTask().getPatient());

		return updateNotificationsOnRoleChange(associatedContact.getTask(), associatedContact);
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

		for (AssociatedContact contact : task.getContacts()) {
			if (contact.getPerson().equals(associatedContact.getPerson()))
				throw new IllegalArgumentException("Already in list");
		}

		task.getContacts().add(associatedContact);
		associatedContact.setTask(task);

		associatedContact = associatedContactRepository.save(associatedContact,
				resourceBundle.get("log.contact.add", task, associatedContact),
				associatedContact.getTask().getPatient());

		return new ContactReturn(associatedContact.getTask(), associatedContact);
	}

	/**
	 * Adds a new notification with the given type
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notificationTyp
	 * @return
	 */
	public NotificationReturn addNotificationByType(AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp) {
		return addNotificationByType(associatedContact, notificationTyp, true, false, false, null, null, false);

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
	public NotificationReturn addNotificationByType(AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp, boolean active, boolean performed,
			boolean failed, Date dateOfAction, String customAddress, boolean renewed) {

		logger.debug("Adding notification of type " + notificationTyp);

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

		associatedContact = associatedContactRepository
				.save(associatedContact,
						resourceBundle.get("log.contact.notification.added", associatedContact.getTask(),
								associatedContact, notificationTyp.toString()),
						associatedContact.getTask().getPatient());

		return new NotificationReturn(associatedContact.getTask(), associatedContact, newNotification);
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
	public NotificationReturn addNotificationByTypeAndDisableOld(AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp) {
		associatedContact = markNotificationsAsActive(associatedContact, notificationTyp, false).getAssociatedContact();
		return addNotificationByType(associatedContact, notificationTyp);
	}

	/**
	 * Sets the given notification as inactive an adds a new notification of the
	 * same type (active)
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notification
	 */
	public NotificationReturn renewNotification(AssociatedContact associatedContact,
			AssociatedContactNotification notification) {

		notification.setActive(false);

		associatedContact = associatedContactRepository.save(associatedContact,
				resourceBundle.get("log.contact.notification.inactive", associatedContact.getTask(), associatedContact,
						notification.getNotificationTyp().toString(), "inactive"),
				associatedContact.getTask().getPatient());

		return addNotificationByType(associatedContact, notification.getNotificationTyp(), true, false, false, null,
				notification.getContactAddress(), true);
	}

	/**
	 * Sets all notifications with the given type to the given active status
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notificationTyp
	 * @param active
	 */
	public ContactReturn markNotificationsAsActive(AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp, boolean active) {

		for (AssociatedContactNotification notification : associatedContact.getNotifications()) {
			if (notification.getNotificationTyp().equals(notificationTyp) && notification.isActive()) {
				notification.setActive(active);
				associatedContact = associatedContactRepository.save(associatedContact,
						resourceBundle.get("log.contact.notification.inactive", associatedContact.getTask(),
								associatedContact, notification.getNotificationTyp().toString(),
								active ? "active" : "inactive"),
						associatedContact.getTask().getPatient());
			}
		}

		return new ContactReturn(associatedContact.getTask(), associatedContact);
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
	public NotificationReturn performNotification(AssociatedContact associatedContact,
			AssociatedContactNotification notification, String message, boolean success) {
		notification.setActive(false);

		notification.setPerformed(true);
		notification.setDateOfAction(new Date(System.currentTimeMillis()));
		notification.setCommentary(message);
		// if success = performed, nothing to do = inactive, if failed = active
		notification.setActive(!success);
		// if success = !failed = false
		notification.setFailed(!success);

		notification = associatedContactNotificationRepository.save(notification,
				resourceBundle.get("log.contact.notification.performed", notification.getContact().getTask(),
						notification.getContact(), notification.getNotificationTyp().toString(), success, message),
				associatedContact.getTask().getPatient());

		return new NotificationReturn(null, notification.getContact(), notification);
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
			return physicianRepository.save(p.get(), resourceBundle.get("log.contact.priority.increment",
					p.get().getPerson().getFullName(), p.get().getPriorityCount()));
		}

		return null;

	}

	@Getter
	@Setter
	public static class ContactReturn {
		protected Task task;
		protected AssociatedContact associatedContact;

		public ContactReturn(Task task, AssociatedContact associatedContact) {
			this.task = task;
			this.associatedContact = associatedContact;
		}
	}

	@Getter
	@Setter
	public static class NotificationReturn extends ContactReturn {
		private AssociatedContactNotification associatedContactNotification;

		public NotificationReturn(Task task, AssociatedContact associatedContact,
				AssociatedContactNotification associatedContactNotification) {
			super(task, associatedContact);
			this.associatedContactNotification = associatedContactNotification;
		}
	}

	public static String generateAddress(AssociatedContact associatedContact) {
		return generateAddress(associatedContact, null);
	}

	public static String generateAddress(AssociatedContact associatedContact, Organization organization) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(associatedContact.getPerson().getFullName() + "\r\n");

		Optional<String> addition1;
		Optional<String> addition2;
		Optional<String> street;
		Optional<String> postcode;
		Optional<String> town;

		if (organization != null) {
			street = Optional.ofNullable(organization.getContact().getStreet()).filter(s -> !s.isEmpty());
			postcode = Optional.ofNullable(organization.getContact().getPostcode()).filter(s -> !s.isEmpty());
			town = Optional.ofNullable(organization.getContact().getTown()).filter(s -> !s.isEmpty());
			addition1 = Optional.ofNullable(organization.getContact().getAddressadditon()).filter(s -> !s.isEmpty());
			addition2 = Optional.ofNullable(organization.getContact().getAddressadditon2()).filter(s -> !s.isEmpty());
			buffer.append(organization.getName() + "\r\n");

		} else {
			// no organization is selected or present, so add the data of the
			// user
			street = Optional.ofNullable(associatedContact.getPerson().getContact().getStreet())
					.filter(s -> !s.isEmpty());
			postcode = Optional.ofNullable(associatedContact.getPerson().getContact().getPostcode())
					.filter(s -> !s.isEmpty());
			town = Optional.ofNullable(associatedContact.getPerson().getContact().getTown()).filter(s -> !s.isEmpty());
			addition1 = Optional.ofNullable(associatedContact.getPerson().getContact().getAddressadditon())
					.filter(s -> !s.isEmpty());
			addition2 = Optional.ofNullable(associatedContact.getPerson().getContact().getAddressadditon2())
					.filter(s -> !s.isEmpty());
		}

		buffer.append(addition1.isPresent() ? addition1.get() + "\r\n" : "");
		buffer.append(addition2.isPresent() ? addition2.get() + "\r\n" : "");
		buffer.append(street.isPresent() ? street.get() + "\r\n" : "");
		buffer.append(postcode.isPresent() ? postcode.get() + " " : "");
		buffer.append(town.isPresent() ? town.get() + "\r\n" : "");

		return buffer.toString();
	}

	/**
	 * Returns all active notification that are pending. If ignoreActiveState is
	 * true the last performed notification of that type will be returned even if it
	 * is not active any more.
	 * 
	 * @param task
	 * @param type
	 * @param ignoreActiveState
	 * @return
	 */
	public static List<NotificationContainer> getNotificationListForType(Task task, NotificationTyp type,
			boolean ignoreActiveState) {

		List<NotificationContainer> containers = new ArrayList<NotificationContainer>();

		for (AssociatedContact associatedContact : task.getContacts()) {
			// getting all notifications of this type
			List<AssociatedContactNotification> notification = associatedContact.getNotifications().stream()
					.filter(p -> p.getNotificationTyp() == type).collect(Collectors.toList());

			if (notification.size() > 0) {

				Optional<AssociatedContactNotification> res = notification.stream().filter(p -> p.isActive())
						.findFirst();

				if (res.isPresent()) {
					containers.add(new NotificationContainer(associatedContact, res.get()));
				} else if (ignoreActiveState) {

					// sorting after date, getting the last notification of that type
					notification = notification.stream()
							.sorted((p1, p2) -> p1.getDateOfAction().compareTo(p2.getDateOfAction()))
							.collect(Collectors.toList());
					
					containers.add(
							new NotificationContainer(associatedContact, notification.get(notification.size() - 1)));
				}
			}
		}

		return containers;
	}

}
