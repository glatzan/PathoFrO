package com.patho.main.util.notification

import com.patho.main.model.patient.notification.AssociatedContact
import com.patho.main.model.patient.notification.AssociatedContactNotification

/**
 * Status of the AssociatedContat notifications
 */
open class NotificationStatus(contact: AssociatedContact) {

    /**
     * All notifications are completed
     */
    val performed = contact.isNotificationPerformed()

    val emailNotification = NotificationBearer(contact, AssociatedContactNotification.NotificationTyp.EMAIL)
    val faxNotification = NotificationBearer(contact, AssociatedContactNotification.NotificationTyp.FAX)
    val phoneNotification = NotificationBearer(contact, AssociatedContactNotification.NotificationTyp.PHONE)
    val letterNotification = NotificationBearer(contact, AssociatedContactNotification.NotificationTyp.LETTER)
    val printNotification = NotificationBearer(contact, AssociatedContactNotification.NotificationTyp.PRINT)

    /**
     * Contact
     */
    val contact = contact

    /**
     * Returns all notifications as a list
     */
    val notifications: List<NotificationBearer>
        get() {
            val result: ArrayList<NotificationBearer> = ArrayList<NotificationBearer>(5)
            if (!emailNotification.empty) result.add(emailNotification)
            if (!faxNotification.empty) result.add(faxNotification)
            if (!phoneNotification.empty) result.add(phoneNotification)
            if (!letterNotification.empty) result.add(letterNotification)
            if (!printNotification.empty) result.add(printNotification)
            return result
        }

    /**
     * Bearer for a single notification type
     */
    open class NotificationBearer(contact: AssociatedContact, type: AssociatedContactNotification.NotificationTyp) {

        val type: AssociatedContactNotification.NotificationTyp = type

        val notifications: List<AssociatedContactNotification> = contact.findByNotificationTyp(type)

        val totalAttempts: Int = notifications.count { p -> p.performed }
        val successfulAttempts: Int = notifications.count { p -> p.performed && !p.failed }
        val failedAttempts: Int = notifications.count { p -> p.performed && p.failed }

        val active: Boolean = notifications.any { p -> p.active }
        val activeNotification: AssociatedContactNotification? = notifications.firstOrNull { p -> p.active }

        val empty = notifications.isEmpty()
    }
}