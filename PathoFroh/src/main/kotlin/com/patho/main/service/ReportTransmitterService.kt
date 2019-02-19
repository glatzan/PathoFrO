package com.patho.main.service

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportTransmitter
import com.patho.main.util.notification.NotificationStatus
import org.springframework.stereotype.Service

@Service()
open class ReportTransmitterService {


    /**
     * Returns the status of all contacts
     */
    open fun getNotificationTypeStatus(task: Task): List<NotificationStatus> {
        return task.contacts.map { p -> getNotificationTypeStatus(p) }
    }

    /**
     * Returns the status of all notifications of one contact.
     */
    open fun getNotificationTypeStatus(contact: ReportTransmitter): NotificationStatus {
        return NotificationStatus(contact)
    }
}