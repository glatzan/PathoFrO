package com.patho.main.dialog.notification

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.Task
import com.patho.main.service.ReportService
import com.patho.main.util.dialog.event.NotificationPerformedEvent
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

    override var progressPerStep: Int = 0

    override var completed: Boolean = false

    override var success: Boolean = false

    var delayedStart: Boolean = false

    var sendReport: PDFContainer? = null

    open fun initAndPrepareBean(task: Task, data: ReportIntentExecuteData): PerformNotificationDialog {
        if (initBean(task, data))
            prepareDialog();
        return this;
    }

    open fun initBean(task: Task, data: ReportIntentExecuteData): Boolean {
        super.initBean(task)
        this.data = data
        status = " "
        progress = 0
        progressPerStep = 0
        completed = false
        delayedStart = false
        return true
    }

    override fun initBean(task: Task): Boolean {
        logger.error("Dialog initialization not allowed")
        return false
    }

    open fun startNotification(delayedStart: Boolean = false) {
        this.delayedStart = delayedStart

        if (!delayedStart)
            sendReport = reportService.executeReportNotification(data, this)
    }

    open fun endPhaseAndHide() {
        super.hideDialog(NotificationPerformedEvent())
    }
}