package com.patho.main.dialog.diagnosis

import com.patho.main.action.dialog.diagnosis.CreateDiagnosisRevisionDialog.DiagnosisRevisionContainer
import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.DiagnosisRevisionType
import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.service.DiagnosisService
import com.patho.main.service.impl.SpringContextBridge.Companion.services
import com.patho.main.util.dialog.event.QuickDiagnosisAddEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

/**
 * Dialog for adding new diagnosis revision. This dialog does not feature many option. It is for quickly adding.
 */
@Component()
@Scope(value = "session")
open class QuickDiagnosisRecordDialog @Autowired constructor(
        private val diagnosisService: DiagnosisService,
        private val taskRepository: TaskRepository,
        private val transactionTemplate: TransactionTemplate) : AbstractDialog_(Dialog.DIAGNOSIS_REVISION_ADD) {

    /**
     * Type of the new diagnosis revision
     */
    lateinit var type: DiagnosisRevisionType

    /**
     * Task for that a diagnosis revision should be created
     */
    lateinit var task: Task

    /**
     * If true the existing diagnoses will be renamed
     */
    var renameOldDiagnoses = true

    /**
     * Internal reference, currently not used
     * TODO: use oder remove
     */
    var intern: String = ""

    private val internalReference: String? = null

    open fun initAndPrepareBean(task: Task, diagnosisRevisionType: DiagnosisRevisionType, intern: String = ""): QuickDiagnosisRecordDialog {
        if (initBean(task, diagnosisRevisionType, intern))
            prepareDialog()
        return this
    }

    open fun initBean(task: Task, diagnosisRevisionType: DiagnosisRevisionType, intern : String): Boolean {
        this.task = taskRepository.findByID(task.id, false, true, false, false, false);
        this.type = diagnosisRevisionType
        this.renameOldDiagnoses = true
        this.intern = intern
        return super.initBean()
    }

    open fun createDiagnosisAndHide() {
        transactionTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                if (renameOldDiagnoses) {
                    // generating new names, only applied if renameOldDiagnoses is true
                    val oldRevisions = DiagnosisRevisionContainer.factory(task,
                            ArrayList(Arrays.asList(DiagnosisRevision("", type))), true)

                    task = services().diagnosisService.renameDiagnosisRevisions(task, oldRevisions)
                }
                task = services().diagnosisService.createDiagnosisRevision(task, type, intern)
            }
        })

        MessageHandler.sendGrowlMessagesAsResource("growl.diagnosis.create.success", "growl.diagnosis.create.quickDiagnosis", task.diagnosisRevisions.last().name)
        hideDialog(QuickDiagnosisAddEvent(true))
    }

    override fun hideDialog() {
        hideDialog(QuickDiagnosisAddEvent(false))
    }
}