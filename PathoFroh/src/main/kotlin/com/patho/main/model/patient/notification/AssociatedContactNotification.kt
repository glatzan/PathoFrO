package com.patho.main.model.patient.notification

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.AssociatedContact
import com.patho.main.model.interfaces.ID
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import java.util.*
import javax.persistence.*

/**
 *
 */
@Entity
@SequenceGenerator(name = "associatedcontactnotification_sequencegenerator", sequenceName = "associatedcontactnotification_sequence")
@Audited
@SelectBeforeUpdate(true)
open class AssociatedContactNotification : AbstractPersistable(), ID {

    @Id
    @GeneratedValue(generator = "associatedcontactnotification_sequencegenerator")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @ManyToOne(targetEntity = AssociatedContact::class, fetch = FetchType.LAZY)
    open var contact: AssociatedContact? = null

    @Enumerated(EnumType.STRING)
    open var notificationTyp: NotificationTyp? = null

    @Column
    open var active: Boolean = false

    @Column
    open var performed: Boolean = false

    @Column
    open var failed: Boolean = false

    @Column
    open var renewed: Boolean = false

    @Column(columnDefinition = "VARCHAR")
    open var commentary: String? = null

    @Temporal(TemporalType.DATE)
    open var dateOfAction: Date? = null

    @Column
    open var manuallyAdded: Boolean = false

    @Column(columnDefinition = "VARCHAR")
    open var contactAddress: String? = null

    enum class NotificationTyp {
        EMAIL, FAX, PHONE, LETTER, PRINT
    }
}