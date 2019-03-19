package com.patho.main.model.patient.notification

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ID
import com.vladmihalcea.hibernate.type.array.IntArrayType
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.hibernate.envers.Audited
import javax.persistence.*

/**
 *  Class representing the status of a singe notification.
 */
@Entity
@Audited
@SelectBeforeUpdate(true)
//@TypeDef(name = "ReportHistoryRecord", typeClass = ReportHistoryRecord::class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
open class ReportIntentNotification : AbstractPersistable, ID {

    @Id
    @GeneratedValue(generator = "reportintentnotification_sequencegenerator")
    @SequenceGenerator(name = "reportintentnotification_sequencegenerator", sequenceName = "reportintentnotification_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    /**
     * Parent Contact
     */
    @ManyToOne(targetEntity = ReportIntent::class, fetch = FetchType.LAZY)
    open var contact: ReportIntent? = null

    /**
     * Notification type, e.g. mail
     */
    @Enumerated(EnumType.STRING)
    open var notificationTyp: NotificationTyp? = null

    /**
     * Notification is active
     */
    @Column
    open var active: Boolean = true

//    /**
//     * Notification was executed, this is even true if the notification process failed
//     */
//    @Column
//    open var performed: Boolean = false
//
//    /**
//     * True if notification failed
//     */
//    @Column
//    open var failed: Boolean = false
//
//    /**
//     * True if this is a copied notification.
//     */
//    @Column
//    open var renewed: Boolean = false
//
//    @Column(columnDefinition = "VARCHAR")
//    open var commentary: String? = null

//    /**
//     * Date on which the notification was performed
//     */
//    open var dateOfAction: Instant? = null

    /**
     * True if the notification address was altered by the user
     */
    @Column
    open var manuallyAdded: Boolean = false

    /**
     * Manually altered notification address
     */
    @Column(columnDefinition = "VARCHAR")
    open var contactAddress: String = ""

    /**
     * HistoryData
     */
//    @Column
////    @Type(type = "ReportHistoryRecord")
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    open var history: MutableList<ReportHistoryRecord> = mutableListOf<ReportHistoryRecord>()
        get() {
            if (field == null)
                field = mutableListOf<ReportHistoryRecord>()
            return field
        }

    constructor()

    constructor(contact: ReportIntent, notificationTyp: NotificationTyp, active: Boolean) {
        this.contact = contact
        this.notificationTyp = notificationTyp
        this.active = active
    }

    /**
     * Type of the notification process
     */
    enum class NotificationTyp {
        EMAIL, FAX, PHONE, LETTER, PRINT, NONE
    }
}