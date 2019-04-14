package com.patho.main.dialog.notification

import com.patho.main.action.dialog.AbstractTabDialog
import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTabTaskDialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.template.PrintDocument
import com.patho.main.ui.transformer.DefaultTransformer

class NotificationDialog : AbstractTabTaskDialog(Dialog.NOTIFICATION) {

    val generalTab: GeneralTab = GeneralTab()

    fun initBean(task: Task): Boolean {
        return super.initBean(task, true, null)
    }

    abstract inner class NotificationTab : AbstractTab(){

        /**
         * True if notification method should be used
         */
        protected var useNotification: Boolean = false

        protected var templateList: List<PrintDocument>? = null

        protected var templateListTransformer: DefaultTransformer<PrintDocument>? = null

        protected var selectedTemplate: PrintDocument? = null

        /**
         * Updates the notification container list and if at least one notification for
         * this task should be performed useTab is set to true
         */
        override fun updateData() {}
    }

    inner class GeneralTab : NotificationTab() {

        private var diagnosisRevisions: List<DiagnosisRevision>? = null

        private var selectDiagnosisRevision: DiagnosisRevision? = null

        private var printCount: Int = 0

        /**
         * No diagnosis is selected, selection by user is required
         */
        private var selectDiagnosisManually: Boolean = false

        /**
         * Selected diagnosis is not approved
         */
        private var selectedDiagnosisNotApprovedy: Boolean = false

        /**
         * If true all contacts will be notified, even contacts that are not refreshed
         */
        private var completeNotification: Boolean = false

        init {
            tabName = "GeneralTab"
            name = "dialog.notification.tab.general"
            viewID = "generalTab"
            centerInclude = "include/general.xhtml"
        }

        override fun initTab(): Boolean {
            return true
        }

        override fun updateData() {}

    }

}