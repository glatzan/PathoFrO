package com.patho.main.model

import com.patho.main.model.interfaces.ID
import org.hibernate.annotations.DynamicUpdate
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
@DynamicUpdate(true)
class AssociatedContactNotification : ID {

    @Id
    @GeneratedValue(generator = "associatedcontactnotification_sequencegenerator")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @ManyToOne(targetEntity = AssociatedContact::class, fetch = FetchType.LAZY)
    var contact: AssociatedContact? = null

    @Enumerated(EnumType.STRING)
    var notificationTyp: NotificationTyp? = null

    @Column
    var active: Boolean = false

    @Column
    var performed: Boolean = false

    @Column
    var failed: Boolean = false

    @Column
    var renewed: Boolean = false

    @Column(columnDefinition = "VARCHAR")
    var commentary: String? = null

    @Temporal(TemporalType.DATE)
    var dateOfAction: Date? = null

    @Column
    var manuallyAdded: Boolean = false

    @Column(columnDefinition = "VARCHAR")
    var contactAddress: String? = null

    override fun equals(obj: Any?): Boolean {
        return if (obj is AssociatedContactNotification && obj.id == id) true else super.equals(obj)
    }

    enum class NotificationTyp {
        EMAIL, FAX, PHONE, LETTER, PRINT
    }
}