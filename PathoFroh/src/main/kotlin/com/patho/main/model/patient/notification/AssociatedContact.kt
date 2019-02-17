package com.patho.main.model.patient.notification

import com.patho.main.common.ContactRole
import com.patho.main.model.AbstractPersistable
import com.patho.main.model.Person
import com.patho.main.model.interfaces.ID
import com.patho.main.model.patient.Task
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
open class AssociatedContact : AbstractPersistable, ID {

    @Id
    @GeneratedValue(generator = "associatedcontact_sequencegenerator")
    @SequenceGenerator(name = "associatedcontact_sequencegenerator", sequenceName = "associatedcontact_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    open var task: Task? = null

    @OneToOne(cascade = [CascadeType.DETACH])
    open var person: Person? = null

    @Enumerated(EnumType.STRING)
    open var role = ContactRole.NONE

    @OrderColumn(name = "position")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "contact", cascade = [CascadeType.ALL])
    open var notifications = mutableListOf<AssociatedContactNotification>()

    /**
     * Returns true if a notification was performed
     */
    open val notificationPerformed: Boolean
        @Transient
        get() = notifications.any { p -> p.performed }

    constructor()

    constructor(task: Task, person: Person, contactRole: ContactRole = ContactRole.NONE) {
        this.task = task
        this.person = person
        this.role = contactRole
    }

    open fun findByNotificationTypAndPerformed(type: AssociatedContactNotification.NotificationTyp, performed: Boolean): List<AssociatedContactNotification> {
        return notifications.filter { p -> type == p.notificationTyp && performed == p.performed }
    }

    open fun findByNotificationTypAndActive(type: AssociatedContactNotification.NotificationTyp, active: Boolean): List<AssociatedContactNotification> {
        return notifications.filter { p -> type == p.notificationTyp && active == p.active }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as AssociatedContact

        if (id != other.id) return false
        if (task != other.task) return false
        if (person != other.person) return false
        if (role != other.role) return false
        if (notifications != other.notifications) return false

        return true

        if (obj instanceof AssociatedContact) {
            if (((AssociatedContact) obj).getId() == getId())
                return true;
            // same person with the same role, same object
            if (((AssociatedContact) obj).getPerson().equals(getPerson())
                    && ((AssociatedContact) obj).getRole().equals(getRole()))
                return true;
        }

    }
}