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

		updateNotificationForPhysicalDiagnosisReport(task, associatedContact);
	}

	/**
	 * Checks all diagnoses for physical diagnosis report sending, and checks if the
	 * contact is a affected. If so the contact will be marked.
	 * 
	 * @param task
	 * @param associatedContact
	 */
	public void updateNotificationForPhysicalDiagnosisReport(Task task, AssociatedContact associatedContact) {
		Set<ContactRole> sendLetterTo = new HashSet<ContactRole>();

		// checking if a already a report should be send physically, if so do
		// nothing and return
		if (associatedContact.getNotifications().stream()
				.anyMatch(p -> p.getNotificationTyp().equals(AssociatedContactNotification.NotificationTyp.LETTER)))
			return;

		// collecting roles for which a report should be physically send
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
				addNotificationType(task, associatedContact, AssociatedContactNotification.NotificationTyp.LETTER);
				return;
			}
		}

	}

	/**
	 * Updates all contacts an checks if a physical letter should be send to them
	 * (depending on the selected diagnosis)
	 * 
	 * @param task
	 * @param diagnosisRevision
	 */
	public void updateNotificationsForPhysicalDiagnosisReport(Task task) {
		for (AssociatedContact associatedContact : task.getContacts()) {
			updateNotificationForPhysicalDiagnosisReport(task, associatedContact);
		}
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
	public void removeNotification(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification notification) {

		if (associatedContact.getNotifications() != null) {
			associatedContact.getNotifications().remove(notification);

			// only remove from array, and deleting the entity only (no saving
			// of contact necessary because mapped within notification)
			associatedContactNotificationRepository.delete(notification,
					resourceBundle.get("log.contact.notification.removed", notification.getNotificationTyp().toString(),
							associatedContact.toString()));
		}
	}

	/**
	 * Adds an associated contact
	 * 
	 * @param task
	 * @param associatedContact
	 * @return
	 */
	public AssociatedContact addAssociatedContact(Task task, Person person, ContactRole role) {
		return addAssociatedContact(task, new AssociatedContact(task, person, role));
	}

	/**
	 * Adds an associated contact
	 * 
	 * @param task
	 * @param associatedContact
	 * @return
	 */
	public AssociatedContact addAssociatedContact(Task task, AssociatedContact associatedContact) {

		if (task.getContacts().stream().anyMatch(p -> p.getPerson().getId() == associatedContact.getPerson().getId()))
			throw new IllegalArgumentException("Already in list");

		task.getContacts().add(associatedContact);
		associatedContact.setTask(task);

		associatedContactRepository.save(associatedContact, resourceBundle.get("log.contact.add", associatedContact));

		return associatedContact;
	}

	/**
	 * Adds a new notification with the given type
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notificationTyp
	 * @return
	 */
	public AssociatedContactNotification addNotificationType(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp) {
		return addNotificationType(task, associatedContact, notificationTyp, true, false, false, null, null, false);

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
	public AssociatedContactNotification addNotificationType(Task task, AssociatedContact associatedContact,
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

		associatedContactRepository.save(associatedContact,
				resourceBundle.get("log.contact.notification.added", notificationTyp.toString(), associatedContact));

		return newNotification;
	}

	/**
	 * Sets the given notification as inactive an adds a new notification of the
	 * same type (active)
	 * 
	 * @param task
	 * @param associatedContact
	 * @param notification
	 */
	public AssociatedContactNotification renewNotification(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification notification) {

		notification.setActive(false);

		associatedContactNotificationRepository.save(notification, resourceBundle.get(
				"log.contact.notification.inactive", notification.getNotificationTyp().toString(), associatedContact));

		return addNotificationType(task, associatedContact, notification.getNotificationTyp(), true, false, false, null,
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
	public void setNotificationsAsActive(Task task, AssociatedContact associatedContact,
			AssociatedContactNotification.NotificationTyp notificationTyp, boolean active) {
		for (AssociatedContactNotification notification : associatedContact.getNotifications()) {
			if (notification.getNotificationTyp().equals(notificationTyp) && notification.isActive()) {
				notification.setActive(active);

				associatedContactNotificationRepository.save(notification,
						resourceBundle.get("log.contact.notification.inactive",
								notification.getNotificationTyp().toString(), associatedContact));
			}
		}
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

	public void incrementContactPriorityCounter(Person person) {

		// Create CriteriaBuilder
		CriteriaBuilder qb = getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<Physician> criteria = qb.createQuery(Physician.class);
		Root<Physician> root = criteria.from(Physician.class);
		criteria.select(root);

		Join<Physician, Person> personQuery = root.join(Physician_.person, JoinType.LEFT);

		criteria.where(qb.equal(personQuery.get(Person_.id), person.getId()));
		criteria.distinct(true);

		List<Physician> physicians = getSession().createQuery(criteria).getResultList();

		if (physicians.size() == 1) {

			physicians.get(0).setPriorityCount(physicians.get(0).getPriorityCount() + 1);

			physicianRepository.save(physicians.get(0));
		}

	}

}
