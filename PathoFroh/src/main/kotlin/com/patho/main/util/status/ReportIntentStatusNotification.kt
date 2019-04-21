package com.patho.main.util.status

import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.impl.SpringContextBridge

/**
 * Listing for notification types
 */
open class ReportIntentStatusNotification(var task: Task, val notificationTyp: NotificationTyp, val ignoreActive: Boolean = false) {

    var reportNotificationIntents: MutableList<ReportIntentNotificationBearer> = getReportIntentNotificationBearerList()

    /**
     * Returns the size of the reportNotificationIntents
     */
    val size
        get() = reportNotificationIntents.size

    /**
     * Updates the reportNotificationIntents list. New notification intents will be added, old ones will be updated and
     * removed intents will be removed form the list.
     */
    fun update(task: Task) {
        this.task = task

        var tmpList: MutableList<ReportIntentNotificationBearer> = getReportIntentNotificationBearerList()

        // copy of the current container, all remaining container had been deleted
        val toDeleteContainer: MutableList<ReportIntentNotificationBearer> = reportNotificationIntents.toMutableList()

        // updating or adding new report intents
        tmpList.forEach { p ->
            val toUpdate = reportNotificationIntents.firstOrNull { i -> i.reportIntentNotification == p.reportIntentNotification }
            if (toUpdate != null) {
                toUpdate.reportIntentNotification = p.reportIntentNotification
                // removing found container, so that these won't be deleted
                toDeleteContainer.remove(toUpdate)
            } else {
                reportNotificationIntents.add(p)
            }
        }

        // removing added report intents, so
        reportNotificationIntents.removeAll(toDeleteContainer)


    }

    private fun getReportIntentNotificationBearerList(): MutableList<ReportIntentNotificationBearer> {
        return task.contacts.filter { p -> p.active || ignoreActive }.mapNotNull { p -> SpringContextBridge.services().reportIntentService.findReportIntentNotificationByType(p, notificationTyp)?.let { ReportIntentNotificationBearer(p, it) } }.toMutableList()
    }

    /**
     * Bearer for the notification intent
     */
    open class ReportIntentNotificationBearer(var reportIntent: ReportIntent, var reportIntentNotification: ReportIntentNotification) {

        var contactAddress: String = reportIntentNotification.contactAddress

        /**
         * Status of the reportintentNotification
         */
        val status: ReportIntentBearerStatus = if (reportIntentNotification.active) {
            // status is active, check if history is present, if so check if the notification was successful
            if (SpringContextBridge.services().reportIntentService.isHistoryPresent(reportIntentNotification)) {
                if (SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportIntentNotification))
                    ReportIntentBearerStatus.SUCCESS
                else
                    ReportIntentBearerStatus.FAILED

            } else
                ReportIntentBearerStatus.ACTIVE
            // is inactive
        } else
            ReportIntentBearerStatus.INACTIVE

        var performNotification: Boolean = status == ReportIntentBearerStatus.ACTIVE

        var pdf : PDFContainer? = null

        fun update(update: ReportIntentNotificationBearer) {
            this.reportIntent = update.reportIntent
            this.reportIntentNotification = update.reportIntentNotification
        }
    }

    /**
     * Status for the notification container.
     */
    enum class ReportIntentBearerStatus() {
        // notification is active, no action has been done
        ACTIVE,
        // notification was set to inactive
        INACTIVE,
        // notification was successfully performed
        SUCCESS,
        // notification process failed
        FAILED
    }
}