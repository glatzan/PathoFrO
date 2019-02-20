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
import org.springframework.data.util.ProxyUtils
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
open class ReportTransmitter : AbstractPersistable, ID {

    @Id
    @GeneratedValue(generator = "reporttransmitter_sequencegenerator")
    @SequenceGenerator(name = "reporttransmitter_sequencegenerator", sequenceName = "reporttransmitter_sequence")
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
    open var notifications = mutableListOf<ReportTransmitterNotification>()

    open var history : ReportHistoryJson = ReportHistoryJson()

    constructor()

    constructor(task: Task, person: Person, contactRole: ContactRole = ContactRole.NONE) {
        this.task = task
        this.person = person
        this.role = contactRole
    }

    /**
     * Returns a list of the requested notification type
     */
    @Transient
    open fun findByNotificationTyp(type: ReportTransmitterNotification.NotificationTyp): List<ReportTransmitterNotification> {
        return notifications.filter { p -> p.notificationTyp == type }
    }

    /**
     * Checks if all notifications are performed
     */
    @Transient
    open fun isNotificationPerformed(): Boolean {
        return notifications.all { p -> p.performed }
    }

    /**
     * Returns true if the notification type is performed
     */
    @Transient
    open fun isNotificationPerformed(type: ReportTransmitterNotification.NotificationTyp): Boolean {
        return findByNotificationTyp(type).all { p -> p.performed }
    }

    /**
     * Returns true if any notification is active
     */
    @Transient
    open fun isNotificationActive(): Boolean {
        return notifications.any { p -> p.active }
    }

    /**
     * Returns true if the notification type is active
     */
    @Transient
    open fun isNotificationActive(type: ReportTransmitterNotification.NotificationTyp): Boolean {
        return findByNotificationTyp(type).any { p -> p.active }
    }

    /**
     * Custom equals, checks if person and role is the same
     */
    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true
        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as ReportTransmitter

        if (id == other.id) return true

        // same person with the same role, same object
        if (person == other.person && role == other.role)
            return true

        return false

    }

}
