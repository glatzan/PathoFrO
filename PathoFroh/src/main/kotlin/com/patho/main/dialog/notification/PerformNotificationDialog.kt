package com.patho.main.dialog.notification

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.service.ReportIntentService
import com.patho.main.service.ReportService
import com.patho.main.util.report.NotificationFeedback
import com.patho.main.util.report.ReportIntentExecuteData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class PerformNotificationDialog @Autowired constructor(
        private val reportService: ReportService) : AbstractTaskDialog(Dialog.NOTIFICATION_PERFORM), NotificationFeedback {

    lateinit var data: ReportIntentExecuteData

    override var status: String = ""

    override var progress: Int = 0

    var notificationProgress: Int = 0

    open fun initAndPrepareBean(task: Task, data: ReportIntentExecuteData): PerformNotificationDialog {
        if (initBean(task, data))
            prepareDialog();
        return this;
    }

    open fun initBean(task: Task, data: ReportIntentExecuteData): Boolean {
        super.initBean(task)
        this.data = data
        update(true)
        return true
    }

    override fun initBean(task: Task): Boolean {
        logger.error("Dialog initialization not allowed")
        return false
    }

    open fun startNotification() {
        reportService.executeReportNotification(data, this)
    }

}