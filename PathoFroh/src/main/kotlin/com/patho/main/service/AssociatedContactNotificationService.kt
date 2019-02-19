package com.patho.main.service

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.AssociatedContact
import com.patho.main.util.notification.NotificationStatus
import org.springframework.stereotype.Service

@Service()
open class AssociatedContactNotificationService {


    /**
     * Returns the status of all contacts
     */
    open fun getNotificationTypeStatus(task: Task): List<NotificationStatus> {
        return task.contacts.map { p -> getNotificationTypeStatus(p) }
    }

    /**
     * Returns the status of all notifications of one contact.
     */
    open fun getNotificationTypeStatus(contact: AssociatedContact): NotificationStatus {
        return NotificationStatus(contact)
    }
}