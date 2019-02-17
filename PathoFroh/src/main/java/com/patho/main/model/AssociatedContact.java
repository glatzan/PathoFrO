package com.patho.main.model;

import com.patho.main.common.ContactRole;
import com.patho.main.model.patient.notification.AssociatedContactNotification;
import com.patho.main.model.patient.notification.AssociatedContactNotification.NotificationTyp;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Task;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.CascadeType.ALL;
import static org.hibernate.annotations.LazyCollectionOption.FALSE;

@Entity
@SequenceGenerator(name = "associatedcontact_sequencegenerator", sequenceName = "associatedcontact_sequence")
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
public class AssociatedContact implements ID {

	@ManyToOne(fetch = FetchType.LAZY)
	private Task task;

	@Id
	@GeneratedValue(generator = "associatedcontact_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	/**
	 * All cascade types, but not removing!
	 * 
	 * @return
	 */
	@OneToOne(cascade = { CascadeType.DETACH })
	private Person person;

	@Enumerated(EnumType.STRING)
	private ContactRole role = ContactRole.NONE;

	@OrderColumn(name = "position")
	@LazyCollection(FALSE)
	@OneToMany(mappedBy = "contact", cascade = ALL)
	private List<AssociatedContactNotification> notifications = new ArrayList<AssociatedContactNotification>();

	public AssociatedContact() {
	}

	public AssociatedContact(Task task, Person person) {
		this(task, person, ContactRole.NONE);
	}

	public AssociatedContact(Task task, Person person, ContactRole role) {
		this.person = person;
		this.role = role;
		this.task = task;
	}

	@Transient
	public boolean isNotificationPerformed() {
		if (getNotifications() != null && getNotifications().size() > 0) {
			if (getNotifications().stream().anyMatch(p -> p.getPerformed()))
				return true;
		}
		return false;
	}

	@Transient
	public boolean containsNotificationTyp(NotificationTyp type, boolean performed) {
		return getNotificationTypAsList(type, performed).size() > 0;
	}

	@Transient
	public List<AssociatedContactNotification> getNotificationTypAsList(NotificationTyp type, boolean active) {
		return getNotifications().stream().filter(p -> p.getNotificationTyp().equals(type) && p.getActive() == active)
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		if (getPerson().getFullName() != null && !getPerson().getFullName().isEmpty())
			return getPerson().getFullName();
		else
			return getPerson().getTitle() + " " + getPerson().getFirstName() + " " + getPerson().getLastName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AssociatedContact) {
			if (((AssociatedContact) obj).getId() == getId())
				return true;
			// same person with the same role, same object
			if (((AssociatedContact) obj).getPerson().equals(getPerson())
					&& ((AssociatedContact) obj).getRole().equals(getRole()))
				return true;
		}

		return super.equals(obj);
	}

	public Task getTask() {
		return this.task;
	}

	public long getId() {
		return this.id;
	}

	public Person getPerson() {
		return this.person;
	}

	public ContactRole getRole() {
		return this.role;
	}

	public List<AssociatedContactNotification> getNotifications() {
		return this.notifications;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public void setRole(ContactRole role) {
		this.role = role;
	}

	public void setNotifications(List<AssociatedContactNotification> notifications) {
		this.notifications = notifications;
	}
}
