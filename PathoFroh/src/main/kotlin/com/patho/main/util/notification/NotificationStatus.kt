package com.patho.main.util.notification

import com.patho.main.model.patient.notification.ReportTransmitter
import com.patho.main.model.patient.notification.ReportTransmitterNotification

/**
 * Status of the AssociatedContat notifications
 */
open class NotificationStatus(contact: ReportTransmitter) {

    /**
     * All notifications are completed
     */
    val performed = contact.isNotificationPerformed()

    val emailNotification = NotificationBearer(contact, ReportTransmitterNotification.NotificationTyp.EMAIL)
    val faxNotification = NotificationBearer(contact, ReportTransmitterNotification.NotificationTyp.FAX)
    val phoneNotification = NotificationBearer(contact, ReportTransmitterNotification.NotificationTyp.PHONE)
    val letterNotification = NotificationBearer(contact, ReportTransmitterNotification.NotificationTyp.LETTER)
    val printNotification = NotificationBearer(contact, ReportTransmitterNotification.NotificationTyp.PRINT)

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
    open class NotificationBearer(contact: ReportTransmitter, type: ReportTransmitterNotification.NotificationTyp) {

        val type: ReportTransmitterNotification.NotificationTyp = type

        val notifications: List<ReportTransmitterNotification> = contact.findByNotificationTyp(type)

        val totalAttempts: Int = notifications.count { p -> p.performed }
        val successfulAttempts: Int = notifications.count { p -> p.performed && !p.failed }
        val failedAttempts: Int = notifications.count { p -> p.performed && p.failed }

        val active: Boolean = notifications.any { p -> p.active }
        val activeNotification: ReportTransmitterNotification? = notifications.firstOrNull { p -> p.active }

        val empty = notifications.isEmpty()
    }
}