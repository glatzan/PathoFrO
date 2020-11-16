package com.patho.main.action.views

import com.patho.main.action.handler.MessageHandler
import com.patho.main.action.handler.WorklistHandler
import com.patho.main.common.ContactRole
import com.patho.main.common.GuiCommands
import com.patho.main.dialog.diagnosis.DiagnosisRecordDialog
import com.patho.main.model.Signature
import com.patho.main.model.patient.Diagnosis
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.person.Person
import com.patho.main.model.preset.DiagnosisPreset
import com.patho.main.repository.jpa.PhysicianRepository
import com.patho.main.service.DiagnosisService
import com.patho.main.service.impl.SpringSessionContextBridge
import com.patho.main.util.task.TaskTools
import com.patho.main.util.ui.jsfcomponents.IDiagnosisSelectOverlay
import com.patho.main.util.ui.jsfcomponents.IDiagnosisViewFunction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Scope(value = "session")
open class DiagnosisView @Autowired constructor(
        private val physicianRepository: PhysicianRepository,
        private val diagnosisService: DiagnosisService,
        private val worklistHandler: WorklistHandler,
        private val diagnosisRecordDialog: DiagnosisRecordDialog) : AbstractEditTaskView(), IDiagnosisViewFunction {

    val diagnoses = object : IDiagnosisSelectOverlay {

        override var selectedDiagnosis: DiagnosisPreset? = null

        override val diagnoses: List<DiagnosisPreset>
            get() = SpringSessionContextBridge.services().genericViewData.diagnosisPresets

        override var filter: String = ""

        override fun onShow() {
            selectedDiagnosis = null
            filter = ""
        }
    }

    override fun loadView(task: Task) {
        logger.debug("Loading diagnosis data data")

        for(diagnosisRevision in task.diagnosisRevisions){
            if(diagnosisRevision.notificationStatus == NotificationStatus.NOT_APPROVED)
                diagnosisRevision.signatureDate = LocalDate.now()
        }

        super.loadView(task)
    }

    /**
     * Returns the primary contact for the current role
     */
    fun getPrimaryContactForRole(vararg contactRoles: String): ReportIntent? {
        return TaskTools.getPrimaryContactFromString(task, *contactRoles)
    }

    /**
     * Returns a list of person with the given contact roles
     */
    fun getContactsForRole(vararg contactRoles: String): List<Person> {
        val contactRole = contactRoles.map { p -> ContactRole.valueOf(p) }.toTypedArray()
        return task.contacts.filter { contactRole.contains(it.role) }.mapNotNull { it.person }
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
            diagnosisRecordDialog.initAndPrepareBean(diagnosis)
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
    override fun updatePhysiciansSignature(task: Task, signature: Signature, resourcesKey: String, vararg arr: Any) {
        signature.role = signature?.physician?.clinicRole ?: ""
        save(task, resourcesKey, *arr)
    }


    override fun beginDiagnosisAmendment(diagnosisRevision: DiagnosisRevision) {
        // workaround for forcing a persist of the task, even if no changes have been made
        worklistHandler.current.selectedTaskInfo.admendRevision(diagnosisRevision)
        worklistHandler.current.selectedTask.audit?.updatedOn = System.currentTimeMillis()
        save(worklistHandler.current.selectedTask, "log.patient.task.diagnosisRevision.lock", worklistHandler.current.selectedTask.taskID, diagnosisRevision)
    }

    override fun endDiagnosisAmendment(diagnosisRevision: DiagnosisRevision) {
        // workaround for forcing a persist of the task, even if no changes have been made
        worklistHandler.current.selectedTaskInfo.lockRevision(diagnosisRevision)
        worklistHandler.current.selectedTask.audit?.updatedOn = System.currentTimeMillis()
        val task = diagnosisService.generateDefaultDiagnosisReport(worklistHandler.current.selectedTask, diagnosisRevision)
        save(task, "log.patient.task.diagnosisRevision.lock", task.taskID, diagnosisRevision)
    }
}
