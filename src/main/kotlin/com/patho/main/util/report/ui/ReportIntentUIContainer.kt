package com.patho.main.util.report.ui

import com.patho.main.dialog.notification.NotificationDialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.service.impl.SpringContextBridge

/**
 * Listing for notification types, used by ui
 */
open class ReportIntentUIContainer(var task: Task, val notificationTyp: NotificationTyp, private val contactTab: NotificationDialog.ContactTab, var diagnosisRevision: DiagnosisRevision? = null, val ignoreActive: Boolean = false) {

    var reportNotificationIntents: MutableList<ReportIntentNotificationUIContainer> = getReportIntentNotificationBearerList()

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
     * Returns true if the reportintent has active notifications
     */
    val hasActiveNotifications
        get() = activeNotifications.isNotEmpty()

    /**
     * Updates the reportNotificationIntents list. New notification intents will be added, old ones will be updated and
     * removed intents will be removed form the list.
     */
    fun update(task: Task, diagnosisRevision: DiagnosisRevision? = null) {
        this.task = task
        this.diagnosisRevision = diagnosisRevision

        var tmpList: MutableList<ReportIntentNotificationUIContainer> = getReportIntentNotificationBearerList(task, diagnosisRevision)

        // copy of the current container, all remaining container had been deleted
        val toDeleteContainer: MutableList<ReportIntentNotificationUIContainer> = reportNotificationIntents.toMutableList()

        // updating or adding new report intents
        tmpList.forEach { p ->
            val toUpdate = reportNotificationIntents.firstOrNull { it.reportIntentNotification == p.reportIntentNotification }
            if (toUpdate != null) {
                toUpdate.update(p)
                // removing found container, so that these won'special.pdfOrganizerDialog be deleted
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
    private fun getReportIntentNotificationBearerList(task: Task = this.task, diagnosisRevision: DiagnosisRevision? = this.diagnosisRevision): MutableList<ReportIntentNotificationUIContainer> {
        return task.contacts.filter { p -> p.active || ignoreActive }.mapNotNull { p -> SpringContextBridge.services().reportIntentService.findReportIntentNotificationByType(p, notificationTyp)?.let { ReportIntentNotificationUIContainer(p, it, contactTab, diagnosisRevision) } }.toMutableList()
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
        FAILED,
        // no diagnosis selected
        NO_DIAGNOSIS_SELECTED
    }
}