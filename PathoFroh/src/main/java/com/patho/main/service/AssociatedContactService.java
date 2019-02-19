package com.patho.main.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.patho.main.model.patient.notification.ReportTransmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.ContactRole;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.patient.notification.ReportTransmitterNotification;
import com.patho.main.model.patient.notification.ReportTransmitterNotification.NotificationTyp;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.AssociatedContactNotificationRepository;
import com.patho.main.repository.AssociatedContactRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.util.helper.HistoUtil;
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
	 * @param reportTransmitter
	 */
	public ContactReturn updateNotificationsOnRoleChange(Task task, ReportTransmitter reportTransmitter) {
		logger.debug("Updating Notifications");

		// do nothing if there are some notifications
		if (reportTransmitter.getNotifications() != null && reportTransmitter.getNotifications().size() != 0) {
			return new ContactReturn(reportTransmitter.getTask(), reportTransmitter);
		}

		List<ReportTransmitterNotification.NotificationTyp> types = pathoConfig.getDefaultNotification()
				.getDefaultNotificationForRole(reportTransmitter.getRole());

		for (ReportTransmitterNotification.NotificationTyp notificationTyp : types) {
			reportTransmitter = addNotificationByType(reportTransmitter, notificationTyp).getReportTransmitter();
		}

		return updateNotificationWithDiagnosisPresets(reportTransmitter.getTask(), reportTransmitter);
	}

	/**
	 * Checks all diagnoses for physical diagnosis report sending, and checks if the
	 * contact is a affected. If so the contact will be marked.
	 * 
	 * @param task
	 * @param reportTransmitter
	 */
	public ContactReturn updateNotificationWithDiagnosisPresets(Task task, ReportTransmitter reportTransmitter) {
		Set<ContactRole> sendLetterTo = new HashSet<ContactRole>();

		// checking if a already a report should be send physically, if so do
		// nothing and return
		if (reportTransmitter.getNotifications() != null && reportTransmitter.getNotifications().stream()
				.anyMatch(p -> p.getNotificationTyp().equals(ReportTransmitterNotification.NotificationTyp.LETTER)))
			return new ContactReturn(task, reportTransmitter);

		// collecting roles for which a report should be send by letter
		for (DiagnosisRevision diagnosisRevision : task.getDiagnosisRevisions()) {
			for (Diagnosis diagnosis : diagnosisRevision.getDiagnoses()) {
				if (diagnosis.getDiagnosisPrototype() != null)
					sendLetterTo.addAll(diagnosis.getDiagnosisPrototype().getDiagnosisReportAsLetter());
			}
		}

		// checking if contact is within the send letter to roles
		for (ContactRole contactRole : sendLetterTo) {
			if (reportTransmitter.getRole().equals(contactRole)) {
				// adding notification and return;
				return new ContactReturn(
						addNotificationByType(reportTransmitter, ReportTransmitterNotification.NotificationTyp.LETTER)
								.getTask(),
						reportTransmitter);
			}
		}

		return new ContactReturn(task, reportTransmitter);
	}

	/**
	 * Updates all contacts an checks if a physical letter should be send to them
	 * (depending on the selected diagnosis)
	 * 
	 * @param task
	 */
	public Task updateNotificationsForPhysicalDiagnosisReport(Task task) {
		for (ReportTransmitter reportTransmitter : task.getContacts()) {
			task = updateNotificationWithDiagnosisPresets(task, reportTransmitter).getTask();
		}
		return task;
	}

	/**
	 * removes a contact
	 * 
	 * @param task
	 * @param reportTransmitter
	 */
	public void removeAssociatedContact(Task task, ReportTransmitter reportTransmitter) {
		task.getContacts().remove(reportTransmitter);
		// only associatedContact has to be removed, is the mapping enity
		associatedContactRepository.delete(reportTransmitter,
				resourceBundle.get("log.contact.remove", task, reportTransmitter),
				reportTransmitter.getTask().getPatient());
	}

	/**
	 * removes a notification
	 * 
	 * @param reportTransmitter
	 * @param notification
	 */
	public ContactReturn removeNotification(ReportTransmitter reportTransmitter,
                                            ReportTransmitterNotification notification) {

		if (reportTransmitter.getNotifications() != null) {
			reportTransmitter.getNotifications().remove(notification);

			reportTransmitter = associatedContactRepository.save(reportTransmitter,
					resourceBundle.get("log.contact.notification.removed", reportTransmitter.getTask(),
							reportTransmitter.toString(), notification.getNotificationTyp().toString()),
					reportTransmitter.getTask().getPatient());

			// only remove from array, and deleting the entity only (no saving
			// of contact necessary because mapped within notification)
			associatedContactNotificationRepository.delete(notification);
		}

		return new ContactReturn(reportTransmitter.getTask(), reportTransmitter);
	}

	/**
	 * Adds an associated contact, and checks if notification methods should be
	 * added because of a specific diagnosis
	 * 
	 * @param task
	 * @param reportTransmitter
	 * @return
	 */
	public ContactReturn addAssociatedContactAndAddDefaultNotifications(Task task,
																		ReportTransmitter reportTransmitter) {

		for (ReportTransmitter contact : task.getContacts()) {
			if (contact.getPerson().equals(reportTransmitter.getPerson()))
				throw new IllegalArgumentException("Already in list");
		}

		task.getContacts().add(reportTransmitter);
		reportTransmitter.setTask(task);

		reportTransmitter = associatedContactRepository.save(reportTransmitter,
				resourceBundle.get("log.contact.add", task, reportTransmitter),
				reportTransmitter.getTask().getPatient());

		return updateNotificationsOnRoleChange(reportTransmitter.getTask(), reportTransmitter);
	}

	/**
	 * Adds an associated contact
	 */
	public ContactReturn addAssociatedContact(Task task, Person person, ContactRole role) {
		return addAssociatedContact(task, new ReportTransmitter(task, person, role));
	}

	/**
	 * Adds an associated contact
	 */
	public ContactReturn addAssociatedContact(Task task, ReportTransmitter reportTransmitter) {

		for (ReportTransmitter contact : task.getContacts()) {
			if (contact.getPerson().equals(reportTransmitter.getPerson()))
				throw new IllegalArgumentException("Already in list");
		}

		task.getContacts().add(reportTransmitter);
		reportTransmitter.setTask(task);

		reportTransmitter = associatedContactRepository.save(reportTransmitter,
				resourceBundle.get("log.contact.add", task, reportTransmitter),
				reportTransmitter.getTask().getPatient());

		return new ContactReturn(reportTransmitter.getTask(), reportTransmitter);
	}

	/**
	 * Adds a new notification with the given type
	 */
	public NotificationReturn addNotificationByType(ReportTransmitter reportTransmitter,
                                                    ReportTransmitterNotification.NotificationTyp notificationTyp) {
		return addNotificationByType(reportTransmitter, notificationTyp, true, false, false, null, null, false);

	}

	public NotificationReturn addNotificationByType(ReportTransmitter reportTransmitter,
                                                    ReportTransmitterNotification.NotificationTyp notificationTyp, boolean active, boolean performed,
                                                    boolean failed, Instant dateOfAction, String customAddress, boolean renewed) {
		return addNotificationByType(reportTransmitter, notificationTyp, active, performed, failed, dateOfAction,
				customAddress, renewed, true);
	}

	/**
	 * Adds a new notification with the given type
	 */
	public NotificationReturn addNotificationByType(ReportTransmitter reportTransmitter,
                                                    ReportTransmitterNotification.NotificationTyp notificationTyp, boolean active, boolean performed,
                                                    boolean failed, Instant dateOfAction, String customAddress, boolean renewed, boolean save) {

		logger.debug("Adding notification of type " + notificationTyp);

		ReportTransmitterNotification newNotification = new ReportTransmitterNotification();
		newNotification.setActive(active);
		newNotification.setPerformed(performed);
		newNotification.setDateOfAction(dateOfAction);
		newNotification.setFailed(failed);
		newNotification.setNotificationTyp(notificationTyp);
		newNotification.setContact(reportTransmitter);
		newNotification.setContactAddress(customAddress);
		newNotification.setRenewed(renewed);

		if (reportTransmitter.getNotifications() == null)
			reportTransmitter.setNotifications(new ArrayList<ReportTransmitterNotification>());

		reportTransmitter.getNotifications().add(newNotification);

		if (save)
			reportTransmitter = associatedContactRepository
					.save(reportTransmitter,
							resourceBundle.get("log.contact.notification.added", reportTransmitter.getTask(),
									reportTransmitter, notificationTyp.toString()),
							reportTransmitter.getTask().getPatient());

		// getting last element, this will be the newNotification because savestructure
		// is a list
		newNotification = HistoUtil.getLastElement(reportTransmitter.getNotifications());

		return new NotificationReturn(reportTransmitter.getTask(), reportTransmitter, newNotification);
	}

	/**
	 * Sets all notifications of the given type to inactive, and creats a new
	 * notification of the given type
	 * 
	 * @param reportTransmitter
	 * @param notificationTyp
	 * @return
	 */
	public NotificationReturn addNotificationByTypeAndDisableOld(ReportTransmitter reportTransmitter,
                                                                 ReportTransmitterNotification.NotificationTyp notificationTyp) {
		reportTransmitter = markNotificationsAsActive(reportTransmitter, notificationTyp, false).getReportTransmitter();
		return addNotificationByType(reportTransmitter, notificationTyp);
	}

	public NotificationReturn renewNotification(ReportTransmitter reportTransmitter,
                                                ReportTransmitterNotification notification) {
		return renewNotification(reportTransmitter, notification, true);
	}

	/**
	 * Sets the given notification as inactive an adds a new notification of the
	 * same type (active)
	 * 
	 * @param reportTransmitter
	 * @param notification
	 */
	public NotificationReturn renewNotification(ReportTransmitter reportTransmitter,
                                                ReportTransmitterNotification notification, boolean saven) {

		notification.setActive(false);
		if (saven)
			reportTransmitter = associatedContactRepository.save(reportTransmitter,
					resourceBundle.get("log.contact.notification.inactive", reportTransmitter.getTask(),
							reportTransmitter, notification.getNotificationTyp().toString(), "inactive"),
					reportTransmitter.getTask().getPatient());

		return addNotificationByType(reportTransmitter, notification.getNotificationTyp(), true, false, false, null,
				notification.getContactAddress(), true, saven);
	}

	/**
	 * Sets all notifications with the given type to the given active status
	 * 
	 * @param reportTransmitter
	 * @param notificationTyp
	 * @param active
	 */
	public ContactReturn markNotificationsAsActive(ReportTransmitter reportTransmitter,
                                                   ReportTransmitterNotification.NotificationTyp notificationTyp, boolean active) {

		for (ReportTransmitterNotification notification : reportTransmitter.getNotifications()) {
			if (notification.getNotificationTyp().equals(notificationTyp) && notification.getActive()) {
				notification.setActive(active);
				reportTransmitter = associatedContactRepository.save(reportTransmitter,
						resourceBundle.get("log.contact.notification.inactive", reportTransmitter.getTask(),
								reportTransmitter, notification.getNotificationTyp().toString(),
								active ? "active" : "inactive"),
						reportTransmitter.getTask().getPatient());
			}
		}

		return new ContactReturn(reportTransmitter.getTask(), reportTransmitter);
	}

	/**
	 * Updates a notification when the notification was performed
	 * 
	 * @param reportTransmitter
	 * @param notification
	 * @param message
	 * @param success
	 * @return
	 */
	public NotificationReturn performNotification(ReportTransmitter reportTransmitter,
                                                  ReportTransmitterNotification notification, String message, boolean success) {
		notification.setActive(false);

		notification.setPerformed(true);
		notification.setDateOfAction(Instant.now());
		notification.setCommentary(message);
		// if success = performed, nothing to do = inactive, if failed = active
		notification.setActive(!success);
		// if success = !failed = false
		notification.setFailed(!success);

		notification = associatedContactNotificationRepository.save(notification,
				resourceBundle.get("log.contact.notification.performed", notification.getContact().getTask(),
						notification.getContact(), notification.getNotificationTyp().toString(), success, message),
				reportTransmitter.getTask().getPatient());

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
		protected ReportTransmitter reportTransmitter;

		public ContactReturn(Task task, ReportTransmitter reportTransmitter) {
			this.task = task;
			this.reportTransmitter = reportTransmitter;
		}
	}

	@Getter
	@Setter
	public static class NotificationReturn extends ContactReturn {
		private ReportTransmitterNotification reportTransmitterNotification;

		public NotificationReturn(Task task, ReportTransmitter associatedContact,
                                  ReportTransmitterNotification reportTransmitterNotification) {
			super(task, associatedContact);
			this.reportTransmitterNotification = reportTransmitterNotification;
		}
	}

	public static String generateAddress(ReportTransmitter reportTransmitter) {
		return generateAddress(reportTransmitter, null);
	}

	public static String generateAddress(ReportTransmitter reportTransmitter, Organization organization) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(reportTransmitter.getPerson().getFullName() + "\r\n");

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
			street = Optional.ofNullable(reportTransmitter.getPerson().getContact().getStreet())
					.filter(s -> !s.isEmpty());
			postcode = Optional.ofNullable(reportTransmitter.getPerson().getContact().getPostcode())
					.filter(s -> !s.isEmpty());
			town = Optional.ofNullable(reportTransmitter.getPerson().getContact().getTown()).filter(s -> !s.isEmpty());
			addition1 = Optional.ofNullable(reportTransmitter.getPerson().getContact().getAddressadditon())
					.filter(s -> !s.isEmpty());
			addition2 = Optional.ofNullable(reportTransmitter.getPerson().getContact().getAddressadditon2())
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

		for (ReportTransmitter reportTransmitter : task.getContacts()) {
			// getting all notifications of this type
			List<ReportTransmitterNotification> notification = reportTransmitter.getNotifications().stream()
					.filter(p -> p.getNotificationTyp() == type).collect(Collectors.toList());

			if (notification.size() > 0) {

				Optional<ReportTransmitterNotification> res = notification.stream().filter(p -> p.getActive())
						.findFirst();

				if (res.isPresent()) {
					containers.add(new NotificationContainer(reportTransmitter, res.get()));
				} else if (ignoreActiveState) {

					if (notification.size() > 1)
						// sorting after date, getting the last notification of that type
						notification = notification.stream()
								.sorted((p1, p2) -> p1.getDateOfAction().compareTo(p2.getDateOfAction()))
								.collect(Collectors.toList());

					// container has to be recreated bevore using it
					containers.add(new NotificationContainer(reportTransmitter,
							notification.get(notification.size() - 1), true));
				}
			}
		}

		return containers;
	}

}
