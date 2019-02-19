package com.patho.main.model.patient.notification

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ID
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.envers.Audited
import java.time.Instant
import javax.persistence.*

/**
 *  Class representing the status of a singe notification.
 */
@Entity
@Audited
@SelectBeforeUpdate(true)
@TypeDef(name = "ReportTransmitterNotificationDataJson", typeClass = ReportTransmitterNotificationDataJson::class)
open class ReportTransmitterNotification : AbstractPersistable(), ID {

    @Id
    @GeneratedValue(generator = "reporttransmitternotification_sequencegenerator")
    @SequenceGenerator(name = "reporttransmitternotification_sequencegenerator", sequenceName = "reporttransmitternotification_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    /**
     * Parent Contact
     */
    @ManyToOne(targetEntity = ReportTransmitter::class, fetch = FetchType.LAZY)
    open var contact: ReportTransmitter? = null

    /**
     * Notification type, e.g. mail
     */
    @Enumerated(EnumType.STRING)
    open var notificationTyp: NotificationTyp? = null

    /**
     * Notification is active
     */
    @Column
    open var active: Boolean = false

    /**
     * Notification was executed, this is even true if the notification process failed
     */
    @Column
    open var performed: Boolean = false

    /**
     * True if notification failed
     */
    @Column
    open var failed: Boolean = false

    /**
     * True if this is a copied notification.
     */
    @Column
    open var renewed: Boolean = false

    @Column(columnDefinition = "VARCHAR")
    open var commentary: String? = null

    /**
     * Date on which the notification was performed
     */
    open var dateOfAction: Instant? = null

    /**
     * True if the notification address was altered by the user
     */
    @Column
    open var manuallyAdded: Boolean = false

    /**
     * Manually altered notification address
     */
    @Column(columnDefinition = "VARCHAR")
    open var contactAddress: String? = null

    /**
     * Additional data as json
     */
    @Type(type = "ReportTransmitterNotificationDataJson")
    @Column
    open var additionalData: ReportTransmitterNotificationDataJson? = ReportTransmitterNotificationDataJson()

    /**
     * Type of the notification process
     */
    enum class NotificationTyp {
        EMAIL, FAX, PHONE, LETTER, PRINT
    }
}