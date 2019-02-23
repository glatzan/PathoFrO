package com.patho.main.service;

import com.patho.main.common.ContactRole;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.Organization;
import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.model.patient.notification.ReportIntentNotification;
import com.patho.main.model.patient.notification.ReportIntentNotification.NotificationTyp;
import com.patho.main.repository.AssociatedContactNotificationRepository;
import com.patho.main.repository.AssociatedContactRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.util.notification.NotificationContainer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
     * @param reportIntent
     */
    // updateReportIntentNotificationsWithRole
    public ContactReturn updateNotificationsOnRoleChange(Task task, ReportIntent reportIntent) {
        logger.debug("Updating Notifications");

        // do nothing if there are some notifications
        if (reportIntent.getNotifications() != null && reportIntent.getNotifications().size() != 0) {
            return new ContactReturn(reportIntent.getTask(), reportIntent);
        }

        List<ReportIntentNotification.NotificationTyp> types = pathoConfig.getDefaultNotification()
                .getDefaultNotificationForRole(reportIntent.getRole());

        for (ReportIntentNotification.NotificationTyp notificationTyp : types) {
            reportIntent = addNotificationByType(reportIntent, notificationTyp).getReportIntent();
        }

        return updateNotificationWithDiagnosisPresets(reportIntent.getTask(), reportIntent);
    }

    /**
     * Checks all diagnoses for physical diagnosis report sending, and checks if the
     * contact is a affected. If so the contact will be marked.
     *
     * @param task
     * @param reportIntent
     */
    // updateReportIntentNotificationsWithDiagnosisPresets
    public ContactReturn updateNotificationWithDiagnosisPresets(Task task, ReportIntent reportIntent) {
        Set<ContactRole> sendLetterTo = new HashSet<ContactRole>();

        // checking if a already a report should be send physically, if so do
        // nothing and return
        if (reportIntent.getNotifications() != null && reportIntent.getNotifications().stream()
                .anyMatch(p -> p.getNotificationTyp().equals(ReportIntentNotification.NotificationTyp.LETTER)))
            return new ContactReturn(task, reportIntent);

        // collecting roles for which a report should be send by letter
        for (DiagnosisRevision diagnosisRevision : task.getDiagnosisRevisions()) {
            for (Diagnosis diagnosis : diagnosisRevision.getDiagnoses()) {
                if (diagnosis.getDiagnosisPrototype() != null)
                    sendLetterTo.addAll(diagnosis.getDiagnosisPrototype().getDiagnosisReportAsLetter());
            }
        }

        // checking if contact is within the send letter to roles
        for (ContactRole contactRole : sendLetterTo) {
            if (reportIntent.getRole().equals(contactRole)) {
                // adding notification and return;
                return new ContactReturn(
                        addNotificationByType(reportIntent, ReportIntentNotification.NotificationTyp.LETTER)
                                .getTask(),
                        reportIntent);
            }
        }

        return new ContactReturn(task, reportIntent);
    }

    /**
     * Updates all contacts an checks if a physical letter should be send to them
     * (depending on the selected diagnosis)
     *
     * @param task
     */
    // updateReportIntentNotificationsWithRole
    public Task updateNotificationsForPhysicalDiagnosisReport(Task task) {
        for (ReportIntent reportIntent : task.getContacts()) {
            task = updateNotificationWithDiagnosisPresets(task, reportIntent).getTask();
        }
        return task;
    }

    /**
     * removes a contact
     *
     * @param task
     * @param reportIntent
     */
    //	removeReportIntent
    public void removeAssociatedContact(Task task, ReportIntent reportIntent) {
        task.getContacts().remove(reportIntent);
        // only associatedContact has to be removed, is the mapping enity
        associatedContactRepository.delete(reportIntent,
                resourceBundle.get("log.reportIntent.removed", task, reportIntent),
                reportIntent.getTask().getPatient());
    }

    /**
     * removes a notification
     *
     * @param reportIntent
     * @param notification
     */
    // removeReportIntentNotification
    public ContactReturn removeNotification(ReportIntent reportIntent,
                                            ReportIntentNotification notification) {

        if (reportIntent.getNotifications() != null) {
            reportIntent.getNotifications().remove(notification);

            reportIntent = associatedContactRepository.save(reportIntent,
                    resourceBundle.get("log.reportIntent.notification.removed", reportIntent.getTask(),
                            reportIntent.toString(), notification.getNotificationTyp().toString()),
                    reportIntent.getTask().getPatient());

            // only remove from array, and deleting the entity only (no saving
            // of contact necessary because mapped within notification)
            associatedContactNotificationRepository.delete(notification);
        }

        return new ContactReturn(reportIntent.getTask(), reportIntent);
    }

    /**
     * Adds an associated contact, and checks if notification methods should be
     * added because of a specific diagnosis
     *
     * @param task
     * @param reportIntent
     * @return
     */
    public ContactReturn addAssociatedContactAndAddDefaultNotifications(Task task,
                                                                        ReportIntent reportIntent) {

        for (ReportIntent contact : task.getContacts()) {
            if (contact.getPerson().equals(reportIntent.getPerson()))
                throw new IllegalArgumentException("Already in list");
        }

        task.getContacts().add(reportIntent);
        reportIntent.setTask(task);

        reportIntent = associatedContactRepository.save(reportIntent,
                resourceBundle.get("log.reportIntent.add", task, reportIntent),
                reportIntent.getTask().getPatient());

        return updateNotificationsOnRoleChange(reportIntent.getTask(), reportIntent);
    }

//	/**
//	 * Adds an associated contact
//	 */
//	public ContactReturn addAssociatedContact(Task task, Person person, ContactRole role) {
//		return addAssociatedContact(task, new ReportIntent(task, person, role));
//	}
//
//	/**
//	 * Adds an associated contact
//	 */
	public ContactReturn addAssociatedContact(Task task, ReportIntent reportIntent) {

		for (ReportIntent contact : task.getContacts()) {
			if (contact.getPerson().equals(reportIntent.getPerson()))
				throw new IllegalArgumentException("Already in list");
		}

		task.getContacts().add(reportIntent);
		reportIntent.setTask(task);

		reportIntent = associatedContactRepository.save(reportIntent,
				resourceBundle.get("log.reportIntent.add", task, reportIntent),
				reportIntent.getTask().getPatient());

		return new ContactReturn(reportIntent.getTask(), reportIntent);
	}

    /**
     * Adds a new notification with the given type
     */
    public NotificationReturn addNotificationByType(ReportIntent reportIntent,
                                                    ReportIntentNotification.NotificationTyp notificationTyp) {
        return addNotificationByType(reportIntent, notificationTyp, true, false, false, null, null, false);

    }

    public NotificationReturn addNotificationByType(ReportIntent reportIntent,
                                                    ReportIntentNotification.NotificationTyp notificationTyp, boolean active, boolean performed,
                                                    boolean failed, Instant dateOfAction, String customAddress, boolean renewed) {
        return addNotificationByType(reportIntent, notificationTyp, active, performed, failed, dateOfAction,
                customAddress, renewed, true);
    }

//	/**
//	 * Adds a new notification with the given type
//	 */
//	public NotificationReturn addNotificationByType(ReportIntent reportIntent,
//													ReportIntentNotification.NotificationTyp notificationTyp, boolean active, boolean performed,
//													boolean failed, Instant dateOfAction, String customAddress, boolean renewed, boolean save) {
//
//		logger.debug("Adding notification of type " + notificationTyp);
//
//		ReportIntentNotification newNotification = new ReportIntentNotification();
//		newNotification.setActive(active);
//		newNotification.setPerformed(performed);
//		newNotification.setDateOfAction(dateOfAction);
//		newNotification.setFailed(failed);
//		newNotification.setNotificationTyp(notificationTyp);
//		newNotification.setContact(reportIntent);
//		newNotification.setContactAddress(customAddress);
//		newNotification.setRenewed(renewed);
//
//		if (reportIntent.getNotifications() == null)
//			reportIntent.setNotifications(new ArrayList<ReportIntentNotification>());
//
//		reportIntent.getNotifications().add(newNotification);
//
//		if (save)
//			reportIntent = associatedContactRepository
//					.save(reportIntent,
//							resourceBundle.get("log.reportIntent.notification.add", reportIntent.getTask(),
//									reportIntent, notificationTyp.toString()),
//							reportIntent.getTask().getPatient());
//
//		// getting last element, this will be the newNotification because savestructure
//		// is a list
//		newNotification = HistoUtil.getLastElement(reportIntent.getNotifications());
//
//		return new NotificationReturn(reportIntent.getTask(), reportIntent, newNotification);
//	}

    /**
     * Sets all notifications of the given type to inactive, and creats a new
     * notification of the given type
     *
     * @param reportIntent
     * @param notificationTyp
     * @return
     */
    public NotificationReturn addNotificationByTypeAndDisableOld(ReportIntent reportIntent,
                                                                 ReportIntentNotification.NotificationTyp notificationTyp) {
        reportIntent = markNotificationsAsActive(reportIntent, notificationTyp, false).getReportIntent();
        return addNotificationByType(reportIntent, notificationTyp);
    }

    public NotificationReturn renewNotification(ReportIntent reportIntent,
                                                ReportIntentNotification notification) {
        return renewNotification(reportIntent, notification, true);
    }

    /**
     * Sets the given notification as inactive an adds a new notification of the
     * same type (active)
     *
     * @param reportIntent
     * @param notification
     */
    public NotificationReturn renewNotification(ReportIntent reportIntent,
                                                ReportIntentNotification notification, boolean saven) {

        notification.setActive(false);
        if (saven)
            reportIntent = associatedContactRepository.save(reportIntent,
                    resourceBundle.get("log.contact.notification.inactive", reportIntent.getTask(),
                            reportIntent, notification.getNotificationTyp().toString(), "inactive"),
                    reportIntent.getTask().getPatient());

        return addNotificationByType(reportIntent, notification.getNotificationTyp(), true, false, false, null,
                notification.getContactAddress(), true, saven);
    }

    /**
     * Sets all notifications with the given type to the given active status
     *
     * @param reportIntent
     * @param notificationTyp
     * @param active
     */
    public ContactReturn markNotificationsAsActive(ReportIntent reportIntent,
                                                   ReportIntentNotification.NotificationTyp notificationTyp, boolean active) {

        for (ReportIntentNotification notification : reportIntent.getNotifications()) {
            if (notification.getNotificationTyp().equals(notificationTyp) && notification.getActive()) {
                notification.setActive(active);
                reportIntent = associatedContactRepository.save(reportIntent,
                        resourceBundle.get("log.contact.notification.inactive", reportIntent.getTask(),
                                reportIntent, notification.getNotificationTyp().toString(),
                                active ? "active" : "inactive"),
                        reportIntent.getTask().getPatient());
            }
        }

        return new ContactReturn(reportIntent.getTask(), reportIntent);
    }

    /**
     * Updates a notification when the notification was performed
     *
     * @param reportIntent
     * @param notification
     * @param message
     * @param success
     * @return
     */
    public NotificationReturn performNotification(ReportIntent reportIntent,
                                                  ReportIntentNotification notification, String message, boolean success) {
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
                reportIntent.getTask().getPatient());

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
            return new ContactRole[]{ContactRole.NONE};
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
        protected ReportIntent reportIntent;

        public ContactReturn(Task task, ReportIntent reportIntent) {
            this.task = task;
            this.reportIntent = reportIntent;
        }
    }

    @Getter
    @Setter
    public static class NotificationReturn extends ContactReturn {
        private ReportIntentNotification reportIntentNotification;

        public NotificationReturn(Task task, ReportIntent associatedContact,
                                  ReportIntentNotification reportIntentNotification) {
            super(task, associatedContact);
            this.reportIntentNotification = reportIntentNotification;
        }
    }

    public static String generateAddress(ReportIntent reportIntent) {
        return generateAddress(reportIntent, null);
    }

    public static String generateAddress(ReportIntent reportIntent, Organization organization) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(reportIntent.getPerson().getFullName() + "\r\n");

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
            street = Optional.ofNullable(reportIntent.getPerson().getContact().getStreet())
                    .filter(s -> !s.isEmpty());
            postcode = Optional.ofNullable(reportIntent.getPerson().getContact().getPostcode())
                    .filter(s -> !s.isEmpty());
            town = Optional.ofNullable(reportIntent.getPerson().getContact().getTown()).filter(s -> !s.isEmpty());
            addition1 = Optional.ofNullable(reportIntent.getPerson().getContact().getAddressadditon())
                    .filter(s -> !s.isEmpty());
            addition2 = Optional.ofNullable(reportIntent.getPerson().getContact().getAddressadditon2())
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

        for (ReportIntent reportIntent : task.getContacts()) {
            // getting all notifications of this type
            List<ReportIntentNotification> notification = reportIntent.getNotifications().stream()
                    .filter(p -> p.getNotificationTyp() == type).collect(Collectors.toList());

            if (notification.size() > 0) {

                Optional<ReportIntentNotification> res = notification.stream().filter(p -> p.getActive())
                        .findFirst();

                if (res.isPresent()) {
                    containers.add(new NotificationContainer(reportIntent, res.get()));
                } else if (ignoreActiveState) {

                    if (notification.size() > 1)
                        // sorting after date, getting the last notification of that type
                        notification = notification.stream()
                                .sorted((p1, p2) -> p1.getDateOfAction().compareTo(p2.getDateOfAction()))
                                .collect(Collectors.toList());

                    // container has to be recreated bevore using it
                    containers.add(new NotificationContainer(reportIntent,
                            notification.get(notification.size() - 1), true));
                }
            }
        }

        return containers;
    }

}
