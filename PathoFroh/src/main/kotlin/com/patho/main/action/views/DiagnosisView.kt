package com.patho.main.action.views

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorklistHandler
import com.patho.main.common.GuiCommands
import com.patho.main.model.DiagnosisPreset
import com.patho.main.model.patient.Diagnosis
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.repository.PhysicianRepository
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

    open var diagnosisViewData = listOf<DiagnosisViewData>()

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
    open var selectedDiagnosis : Diagnosis? = null

    override fun loadView(task: Task) {
        logger.debug("Loading reportIntent data")
        super.loadView(task)

        diagnosisViewData = task.diagnosisRevisions.map { p -> DiagnosisViewData(p) }

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
            worklistHandler.replaceTaskInWorklist(t,true, false)
            return
        } else if (diagnosis.diagnosisPrototype != null) {
            logger.debug("Extended diagnosis text found, showing dialog")
            MessageHandler.executeScript(GuiCommands.OPEN_COPY_HISTOLOGICAL_RECORD_DIALOG_FROM_DIAGNOSIS_VIEW)
        }
    }


//    /**
//     * Updates a reportIntent with a preset
//     *
//     * @param reportIntent
//     * @param preset
//     */
//    fun updateDiagnosisPrototype(reportIntent: Diagnosis, preset: DiagnosisPreset) {
//        logger.debug("Updating reportIntent with prototype")
//        val task = diagnosisService.updateDiagnosisWithPrototype(reportIntent.task, reportIntent, preset)
//
//        globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS)
//    }
//
//    /**
//     * Updates a reportIntent without a preset. (Removes the previously set preset)
//     */
//    fun updateDiagnosisPrototype(reportIntent: Diagnosis, diagnosisAsText: String) {
//        updateDiagnosisPrototype(reportIntent, diagnosisAsText, "", reportIntent.malign, "")
//    }
//
//    /**
//     * Updates a reportIntent without a preset. (Removes the previously set preset)
//     */
//    fun updateDiagnosisPrototype(reportIntent: Diagnosis, diagnosisAsText: String, extendedDiagnosisText: String,
//                                 malign: Boolean, icd10: String) {
//        logger.debug("Updating reportIntent to $diagnosisAsText")
//        setSelectedTask(diagnosisService.updateDiagnosisWithoutPrototype(reportIntent.task, reportIntent,
//                diagnosisAsText, extendedDiagnosisText, malign, icd10))
//        globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS)
//    }


    open class DiagnosisViewData(diagnosisRevision: DiagnosisRevision) {
        open val diagnosisRevision = diagnosisRevision
    }
}