package com.patho.main.action.views

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorklistHandler
import com.patho.main.common.GuiCommands
import com.patho.main.model.Signature
import com.patho.main.model.patient.Diagnosis
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.system.DiagnosisPreset
import com.patho.main.repository.jpa.PhysicianRepository
import com.patho.main.service.DiagnosisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope(value = "session")
open class DiagnosisView @Autowired constructor(
        private val physicianRepository: PhysicianRepository,
        private val diagnosisService: DiagnosisService,
        private val worklistHandler: WorklistHandler) : AbstractEditTaskView() {

    /**
     * Array for diagnoses filter
     */
    open var diagnosisFilter: Array<Array<String>> = arrayOf<Array<String>>()

    /**
     * Array for selected reportIntent presets
     */
    open var selectedDiagnosisPresets: Array<Array<DiagnosisPreset?>> = arrayOf<Array<DiagnosisPreset?>>()

    /**
     * Diagnosis for copyFing the diagnosis text
     */
    open var selectedDiagnosis: Diagnosis? = null

    override fun loadView(task: Task) {
        logger.debug("Loading reportIntent data")
        super.loadView(task)

        // updating signature date and person to sign
        for (revision in task.diagnosisRevisions) {
            if (!revision.completed) {
                revision.signatureDate = LocalDate.now()

                if (revision.signatureOne.physician == null || revision.signatureTwo.physician == null) {
                    // TODO set if physician to the left, if consultant to the right
                }
            }
        }

        diagnosisFilter = arrayOf<Array<String>>()
        selectedDiagnosisPresets = arrayOf<Array<DiagnosisPreset?>>()

        for (revision in task.diagnosisRevisions) {
            diagnosisFilter += Array<String>(revision.diagnoses.size) { "" }
            selectedDiagnosisPresets += Array<DiagnosisPreset?>(revision.diagnoses.size) { null }
        }
    }

    /**
     * Copies the reportIntent record to the reportIntent if no text was entered. If task
     * was provided before, a dialog will be opened.
     */
    fun copyHistologicalRecord(diagnosis: Diagnosis?) {
        diagnosis ?: return

        // setting diagnosistext if no text is set
        if (diagnosis.parent!!.text.isEmpty() && diagnosis.diagnosisPrototype != null) {
            logger.debug("No extended diagnosis text found, text copied")
            val t = diagnosisService.copyHistologicalRecord(diagnosis, true)
            worklistHandler.replaceTaskInWorklist(t, true, false)
            return
        } else if (diagnosis.diagnosisPrototype != null) {
            logger.debug("Extended diagnosis text found, showing dialog")
            MessageHandler.executeScript(GuiCommands.OPEN_COPY_HISTOLOGICAL_RECORD_DIALOG_FROM_DIAGNOSIS_VIEW)
        }
    }

    /**
     * Updates a diagnosis with a prototype
     */
    fun updateDiagnosisPrototype(diagnosis: Diagnosis, preset: DiagnosisPreset) {
        logger.debug("Updating reportIntent with prototype")
        val t = diagnosisService.updateDiagnosisWithPrototype(diagnosis.task!!, diagnosis, preset)
        worklistHandler.replaceTaskInWorklist(t, true, false)
    }

    /**
     * Updates a reportIntent without a preset. (Removes the previously set preset)
     */
    fun updateDiagnosisPrototype(diagnosis: Diagnosis, diagnosisAsText: String) {
        val t = diagnosisService.updateDiagnosisWithoutPrototype(diagnosis.task!!, diagnosis, diagnosisAsText, "", diagnosis.malign, "")
        worklistHandler.replaceTaskInWorklist(t, true, false)
    }

    /**
     * Updates the signatures role
     */
    fun updatePhysiciansSignature(task: Task, signature: Signature, resourcesKey: String, vararg arr: Any) {
        signature.role = signature?.physician?.clinicRole ?: ""
        save(task, resourcesKey, *arr)
    }


    open fun beginDiagnosisAmendment(diagnosisRevision: DiagnosisRevision) {
        // workaround for forcing a persist of the task, even if no changes have been made
        worklistHandler.current.selectedTaskInfo.admendRevision(diagnosisRevision)
        worklistHandler.current.selectedTask.audit?.updatedOn = System.currentTimeMillis()
        save(worklistHandler.current.selectedTask, "log.patient.task.diagnosisRevision.lock", worklistHandler.current.selectedTask.taskID, diagnosisRevision)
    }

    open fun endDiagnosisAmendment(diagnosisRevision: DiagnosisRevision) {
        // workaround for forcing a persist of the task, even if no changes have been made
        worklistHandler.current.selectedTaskInfo.lockRevision(diagnosisRevision)
        worklistHandler.current.selectedTask.audit?.updatedOn = System.currentTimeMillis()
        val task = diagnosisService.generateDefaultDiagnosisReport(worklistHandler.current.selectedTask, diagnosisRevision)
        save(task, "log.patient.task.diagnosisRevision.lock", task.taskID, diagnosisRevision)
    }
}