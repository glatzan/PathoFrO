package com.patho.main.dialog.notification

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntentNotification
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
class PerformNotificationDialog : AbstractTaskDialog(Dialog.NOTIFICATION_PERFORM) {

    var reportIntentNotifications: List<ReportIntentNotification> = listOf()

    var notificationProgress: Int = 0

    fun initAndPrepareBean(task: Task, reportIntentNotifications: List<ReportIntentNotification>): PerformNotificationDialog {
        if (initBean(task, reportIntentNotifications))
            prepareDialog();
        return this;
    }

    fun initBean(task: Task, reportIntentNotifications: List<ReportIntentNotification>): Boolean {
        super.initBean(task)
        this.reportIntentNotifications = reportIntentNotifications
        update(true)
        return true
    }

    override fun initBean(task: Task): Boolean {
        logger.error("Dialog initialization not allowed")
        return false
    }

}