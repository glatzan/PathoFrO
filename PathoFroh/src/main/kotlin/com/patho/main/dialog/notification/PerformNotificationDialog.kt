package com.patho.main.dialog.notification

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.Task
import com.patho.main.service.ReportService
import com.patho.main.service.WorkPhaseService
import com.patho.main.util.dialog.event.NotificationPerformedEvent
import com.patho.main.util.report.NotificationFeedback
import com.patho.main.util.report.ReportIntentExecuteData
import com.patho.main.util.ui.backend.CommandButtonStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Dialog for performing the notification with live feedback
 */
@Component()
@Scope(value = "session")
open class PerformNotificationDialog @Autowired constructor(
        private val reportService: ReportService,
        private val workPhaseService: WorkPhaseService) : AbstractTaskDialog(Dialog.NOTIFICATION_PERFORM), NotificationFeedback {

    /**
     * Execute data for performing the notification
     */
    lateinit var data: ReportIntentExecuteData

    /**
     * Current status
     */
    override var status: String = ""

    /**
     * Current progression in percent
     */
    override var progress: Int = 0

    /**
     * Progression in percent per step
     */
    override var progressPerStep: Int = 0

    /**
     * True if notification is completed
     */
    override var completed: Boolean = false

    /**
     * True if notification was successful
     */
    override var success: Boolean = false

    /**
     * If true the start of the poll element is delayed
     */
    var delayedStart: Boolean = false

    /**
     * If notification was performed the generated sendreport will be stored here
     */
    var sendReport: PDFContainer? = null

    /**
     * Commandbutton for opening the pdforganizer in oder to show the send report
     */
    val sendReportButton: CommandButtonStatus = object : CommandButtonStatus() {
        override var isDisabled: Boolean
            get() = !completed && sendReport == null
            set(value) {}
    }

    /**
     * Endphase button
     */
    var endPhaseButton: CommandButtonStatus = object : CommandButtonStatus() {
        override var isDisabled: Boolean
            get() = !completed
            set(value) {}
    }

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
        sendReport = null
        return true
    }

    override fun initBean(task: Task): Boolean {
        logger.error("Dialog initialization not allowed")
        return false
    }

    /**
     * Starts the notification
     */
    open fun startNotification(delayedStart: Boolean = false) {
        this.delayedStart = delayedStart

        if (!delayedStart)
            sendReport = reportService.executeReportNotification(data, this)
    }

    override fun end(success: Boolean) {
        this.success = success
        this.completed = true

        if(success){
            data.diagnosisRevision
        }
    }

    /**
     * Closes the dialog an returns a NotificationPerformedEvent object indicating the phase end
     */
    open fun endPhaseAndHide() {
        super.hideDialog(NotificationPerformedEvent(true))
    }

    /**
     * Closes the dialog and returns a NotificationPerformedEvent object with endphase = false
     */
    override fun hideDialog() {
        super.hideDialog(NotificationPerformedEvent(false))
    }
}