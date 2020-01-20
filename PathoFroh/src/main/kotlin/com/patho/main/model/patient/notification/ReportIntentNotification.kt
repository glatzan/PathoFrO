package com.patho.main.model.patient.notification

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ID
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.envers.Audited
import javax.persistence.*

/**
 *  Class representing the status of a singe notification.
 */
@Entity
@Audited
@SelectBeforeUpdate(true)
//@TypeDef(name = "DiagnosisHistoryRecord", typeClass = DiagnosisHistoryRecord::class)
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
    open var notificationTyp: NotificationTyp = NotificationTyp.NONE

    /**
     * Notification is active
     */
    @Column
    open var active: Boolean = true

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
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    open var history: MutableList<DiagnosisHistoryRecord> = mutableListOf<DiagnosisHistoryRecord>()
        get() {
            if (field == null)
                field = mutableListOf<DiagnosisHistoryRecord>()
            return field
        }

    constructor()

    constructor(contact: ReportIntent, notificationTyp: NotificationTyp, active: Boolean) {
        this.contact = contact
        this.notificationTyp = notificationTyp
        this.active = active
    }
}


