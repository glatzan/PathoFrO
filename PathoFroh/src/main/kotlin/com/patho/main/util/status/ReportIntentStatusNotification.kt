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
     * Returns all active notifications
     */
    val activeNotifications
        get() = reportNotificationIntents.filter { it.performNotification }

    /**
     * Updates the reportNotificationIntents list. New notification intents will be added, old ones will be updated and
     * removed intents will be removed form the list.
     */
    fun update(task: Task) {
        this.task = task

        var tmpList: MutableList<ReportIntentNotificationBearer> = getReportIntentNotificationBearerList(task)

        // copy of the current container, all remaining container had been deleted
        val toDeleteContainer: MutableList<ReportIntentNotificationBearer> = reportNotificationIntents.toMutableList()

        // updating or adding new report intents
        tmpList.forEach { p ->
            val toUpdate = reportNotificationIntents.firstOrNull { it.reportIntentNotification == p.reportIntentNotification }
            if (toUpdate != null) {
                toUpdate.update(p)
                // removing found container, so that these won't be deleted
                toDeleteContainer.remove(toUpdate)
            } else {
                reportNotificationIntents.add(p)
            }
        }

        // removing added report intents, so
        reportNotificationIntents.removeAll(toDeleteContainer)


    }

    /**
     * Returns a filtered list auf reportintentnotification bearers. In default mode only active container will be returned
     */
    private fun getReportIntentNotificationBearerList(task: Task = this.task): MutableList<ReportIntentNotificationBearer> {
        return task.contacts.filter { p -> p.active || ignoreActive }.mapNotNull { p -> SpringContextBridge.services().reportIntentService.findReportIntentNotificationByType(p, notificationTyp)?.let { ReportIntentNotificationBearer(p, it) } }.toMutableList()
    }

    /**
     * Bearer for the notification intent
     */
    open class ReportIntentNotificationBearer(var reportIntent: ReportIntent, var reportIntentNotification: ReportIntentNotification) {

        /**
         * Conatct address, if reportIntentNotification has no contact address set, a new one will be generated
         */
        var contactAddress: String = if (reportIntentNotification.contactAddress.isEmpty()) getContactAddress(reportIntentNotification) else reportIntentNotification.contactAddress

        /**
         * Status of the reportintentNotification
         */
        val status: ReportIntentBearerStatus
            get() = if (reportIntentNotification.active) {
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

        /**
         * If true a notification is performed
         */
        var performNotification: Boolean = status == ReportIntentBearerStatus.ACTIVE

        /**
         * Custom pdf for contact
         */
        var pdf: PDFContainer? = null

        /**
         * True if the disable button should be rendered
         */
        val renderInactiveButton
            get() = status != ReportIntentBearerStatus.INACTIVE && status != ReportIntentBearerStatus.SUCCESS

        /**
         * True if the enable button should be rendered
         */
        val renderActiveButton
            get() = status == ReportIntentBearerStatus.INACTIVE


        fun update(update: ReportIntentNotificationBearer) {
            this.reportIntent = update.reportIntent
            this.reportIntentNotification = update.reportIntentNotification
            this.performNotification = status == ReportIntentBearerStatus.ACTIVE
        }

        /**
         * Returns the matching contact address for the reportIntentNotification
         */
        private fun getContactAddress(reportIntentNotification: ReportIntentNotification): String {
            return when (reportIntentNotification.notificationTyp) {
                NotificationTyp.EMAIL -> reportIntentNotification.contact?.person?.contact?.email ?: ""
                NotificationTyp.FAX -> reportIntentNotification.contact?.person?.contact?.fax ?: ""
                NotificationTyp.PHONE -> reportIntentNotification.contact?.person?.contact?.phone ?: ""
                NotificationTyp.LETTER -> reportIntentNotification.contact?.let { SpringContextBridge.services().reportIntentService.generateAddress(it) }
                        ?: ""
                else -> ""
            }
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