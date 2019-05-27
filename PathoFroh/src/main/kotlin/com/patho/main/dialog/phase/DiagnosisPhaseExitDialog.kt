package com.patho.main.dialog.phase

import com.patho.main.common.Dialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.repository.TaskRepository
import com.patho.main.util.status.diagnosis.DiagnosisBearer
import com.patho.main.util.status.diagnosis.IReportIntentStatusByDiagnosisViewData
import com.patho.main.util.status.diagnosis.ReportIntentStatusByDiagnosis
import com.patho.main.util.task.TaskNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Dialog for leaving the reportIntent phase
 */
@Component()
@Scope(value = "session")
open class DiagnosisPhaseExitDialog @Autowired constructor(
        private val taskRepository: TaskRepository) : AbstractPhaseExitDialog(Dialog.DIAGNOSIS_PHASE_EXIT), IReportIntentStatusByDiagnosisViewData {

    /**
     * List of all reportIntent revisions with their status
     */
    override lateinit var diagnosisRevisions: ReportIntentStatusByDiagnosis

    /**
     * Selected reportIntent for that the notification will be performed
     */
    override var selectDiagnosisRevision: DiagnosisBearer? = null

    /**
     * Diagnosis bearer for displaying details in the datatable overlay panel
     */
    override var viewDiagnosisRevisionDetails: DiagnosisBearer? = null

    open fun initBean(task: Task, diagnosisRevision: DiagnosisRevision) {
        val optionalTask = taskRepository.findOptionalByIdAndInitialize(task.id, true, true, false, true, true)

        if (!optionalTask.isPresent)
            throw TaskNotFoundException()

        val oTask = optionalTask.get()

        diagnosisRevisions = ReportIntentStatusByDiagnosis(task)
        selectDiagnosisRevision = diagnosisRevisions.diagnosisBearer.firstOrNull { p -> p.diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_PENDING }

        super.initBean(task)
    }
}