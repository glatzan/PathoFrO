package com.patho.main.service

import com.patho.main.action.dialog.diagnosis.CreateDiagnosisRevisionDialog
import com.patho.main.common.DiagnosisRevisionType
import com.patho.main.model.DiagnosisPreset
import com.patho.main.model.Signature
import com.patho.main.model.patient.*
import com.patho.main.repository.DiagnosisRepository
import com.patho.main.repository.DiagnosisRevisionRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.ui.task.DiagnosisReportUpdater
import com.patho.main.util.exceptions.LastDiagnosisCanNotBeDeletedException
import com.patho.main.util.helper.TaskUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*


@Service
open class DiagnosisService constructor(
        private val taskRepository: TaskRepository,
        private val diagnosisRepository: DiagnosisRepository,
        private val reportIntentService: ReportIntentService,
        private val diagnosisRevisionRepository: DiagnosisRevisionRepository) : AbstractService() {


    /**
     * Updates all diagnosisRevision of the given revisions
     */
    @Transactional
    open fun synchronizeDiagnosesAndSamples(task: Task, save: Boolean): Task {
        logger.info("Synchronize all diagnoses of task ${task.taskID} with samples")

        for (revision in task.diagnosisRevisions) {
            synchronizeDiagnosesAndSamples(revision, task.samples, false)
        }

        return if (save)
            taskRepository.save(task, resourceBundle["log.patient.task.diagnosisRevisions.update"], task.patient)
        else
            task
    }

    /**
     * Updates a diagnosisRevision with a sample list. Used for adding and removing
     * samples after initial revision creation.
     */
    @Transactional
    open fun synchronizeDiagnosesAndSamples(diagnosisRevision: DiagnosisRevision, samples: List<Sample>,
                                            save: Boolean): DiagnosisRevision {
        logger.info("Synchronize reportIntent list with samples")

        val diagnosesInRevision = diagnosisRevision.diagnoses

        val samplesToAddDiagnosis = ArrayList(samples)

        val toRemoveDiagnosis = ArrayList<Diagnosis>()

        outerLoop@ for (diagnosis in diagnosesInRevision) {
            // sample already in diagnosisList, removing from to add array
            for (sample in samplesToAddDiagnosis) {
                if (sample.id == diagnosis.sample!!.id) {
                    samplesToAddDiagnosis.remove(sample)
                    logger.trace("Sample found, Removing sample ${sample.id} from list.")
                    continue@outerLoop
                }
            }
            logger.trace("Diagnosis has no sample, removing reportIntent ${diagnosis.id}")
            // not found within samples, so sample was deleted, deleting
            // reportIntent as well.
            toRemoveDiagnosis.add(diagnosis)
        }

        // removing diagnose if necessary
        for (diagnosis in toRemoveDiagnosis) {
            removeDiagnosis(diagnosis)
        }

        // adding new diagnoses if there are new samples
        for (sample in samplesToAddDiagnosis) {
            logger.trace("Adding new reportIntent for sample ${sample.id}")
            createDiagnosis(diagnosisRevision, sample, false)
        }

        return if (save)
            diagnosisRevisionRepository.save(diagnosisRevision,
                    resourceBundle["log.patient.task.diagnosisRevisions.update", diagnosisRevision], diagnosisRevision.patient)
        else
            diagnosisRevision
    }

    /**
     * Creates a new diagnosis and adds it to the given diagnosisRevision
     */
    @Transactional
    open fun createDiagnosis(revision: DiagnosisRevision, sample: Sample, save: Boolean): Task {
        logger.info("Creating new reportIntent")

        val diagnosis = Diagnosis()
        diagnosis.sample = sample
        diagnosis.parent = revision
        revision.diagnoses.add(diagnosis)

        return if (save)
            taskRepository.save<Task>(revision.task, resourceBundle["log.diagnosis.new", revision, diagnosis], revision.patient)
        else
            revision.task!!
    }

    /**
     * Removes a diagnosis from the parent and deletes it.
     */
    @Transactional
    open fun removeDiagnosis(diagnosis: Diagnosis) {
        logger.info("Removing reportIntent " + diagnosis.name)

        diagnosis.sample = null

        diagnosis.parent!!.diagnoses.remove(diagnosis)

        diagnosisRepository.delete(diagnosis,
                resourceBundle["log.diagnosis.remove", diagnosis.parent, diagnosis], diagnosis.patient)
    }

    /**
     * Creates a diagnosisRevision, adds it to the given task and creates also all
     * needed diagnoses
     */
    @Transactional
    open fun createDiagnosisRevision(task: Task, type: DiagnosisRevisionType): Task {
        return createDiagnosisRevision(task, type, "");
    }

    /**
     * Creates a diagnosisRevision, adds it to the given task and creates also all
     * needed diagnoses
     */
    @Transactional
    open fun createDiagnosisRevision(task: Task, type: DiagnosisRevisionType, internalReference: String): Task {
        return createDiagnosisRevision(task, type,
                TaskUtil.getDiagnosisRevisionName(task.diagnosisRevisions, DiagnosisRevision("", type)),
                internalReference)
    }

    /**
     * Creates a diagnosisRevision, adds it to the given task and creates also all
     * needed diagnoses
     */
    @Transactional
    open fun createDiagnosisRevision(task: Task, type: DiagnosisRevisionType, name: String, internalReference: String): Task {
        logger.info("Creating new diagnosisRevision")

        val diagnosisRevision = DiagnosisRevision()
        diagnosisRevision.type = type
        diagnosisRevision.signatureOne = Signature()
        diagnosisRevision.signatureTwo = Signature()
        diagnosisRevision.name = name
        diagnosisRevision.intern = internalReference

        return addDiagnosisRevision(task, diagnosisRevision)
    }

    /**
     * Adds an reportIntent revision to the task
     */
    @Transactional
    open fun addDiagnosisRevision(task: Task, diagnosisRevision: DiagnosisRevision): Task {
        var task = task
        logger.info("Adding diagnosisRevision to task")
        diagnosisRevision.parent = task
        diagnosisRevision.signatureOne = Signature()
        diagnosisRevision.signatureTwo = Signature()
        task.diagnosisRevisions.add(diagnosisRevision)

        // saving to database
        // diagnosisRevisionRepository.save(diagnosisRevision,
        // resourceBundle.get("log.diagnosisRevision.new",
        // diagnosisRevision));

        // creating a reportIntent for every sample
        for (sample in task.samples) {
            task = createDiagnosis(diagnosisRevision, sample, false)
        }

        // saving to database
        task = taskRepository.save(task, resourceBundle["log.diagnosisRevision.new", diagnosisRevision], task.patient)

        // updating diagnoses with notification intents
        return reportIntentService.updateReportIntentNotificationHistoryWithDiagnoses(task, true)
    }

    /**
     * Deleting a DiagnosisRevision and all included diagnoese
     */
    @Transactional
    open fun removeDiagnosisRevision(task: Task, revision: DiagnosisRevision): Task {
        var task = task
        logger.info("Removing diagnosisRevision ${revision.name}")

        if (task.diagnosisRevisions.size <= 1)
            throw LastDiagnosisCanNotBeDeletedException();

        task.diagnosisRevisions.remove(revision)

        task = taskRepository.save<Task>(revision.parent, resourceBundle["log.diagnosisRevision.delete", revision], task.patient)

        // updating diagnoses with notification intents
        task = reportIntentService.updateReportIntentNotificationHistoryWithDiagnoses(task, true)

        diagnosisRevisionRepository.delete(revision)
        return task
    }

    /**
     * Approves all diagnoses
     */
    @Transactional
    open fun approveAllDiagnoses(task: Task, notificationStatus: NotificationStatus): Task {
        var tmp = task
        for (diagnosisRevision in task.diagnosisRevisions)
            tmp = approveDiagnosis(task, diagnosisRevision, notificationStatus)

        return tmp
    }

    /**
     * Sets a diagnosis as completed
     */
    @Transactional
    open fun approveDiagnosis(task: Task, diagnosisRevision: DiagnosisRevision,
                              notificationStatus: NotificationStatus): Task {
        var tmp = task

        diagnosisRevision.completionDate = Instant.now()
        diagnosisRevision.notificationStatus = notificationStatus

        tmp = taskRepository.save(tmp, resourceBundle["log.diagnosisRevision.approved", diagnosisRevision], tmp.patient)

        return generateDefaultDiagnosisReport(tmp, diagnosisRevision)
    }

    /**
     * Sets the notification status of a diagnosis revision as completed
     */
    @Transactional
    open fun performNotification(task: Task, diagnosisRevision: DiagnosisRevision, success: Boolean = true, save: Boolean = true): Task {
        logger.debug("Notification completed")
        var tmp = task

        diagnosisRevision.notificationStatus = if (success) NotificationStatus.NOTIFICATION_PERFORMED else NotificationStatus.NOTIFICATION_FAILED
        diagnosisRevision.notificationDate = Instant.now()

        if (save)
            tmp = taskRepository.save(tmp, resourceBundle["log.diagnosisRevision.notificationPerformed", diagnosisRevision], tmp.patient)

        return tmp
    }

    /**
     * Sets the notification status to completed, does not alter the notificationDate if that is already set.
     */
    @Transactional
    open fun completeAllNotifications(task: Task, save: Boolean = true): Task {
        var tmp = task
        for (diagnosisRevision in task.diagnosisRevisions)
            tmp = completeNotification(task, diagnosisRevision, true)

        return tmp
    }

    /**
     * Sets the notification status to completed
     */
    @Transactional
    open fun completeNotification(task: Task, diagnosisRevision: DiagnosisRevision, save: Boolean = true): Task {
        var tmp = task

        if (diagnosisRevision.notificationDate == null) diagnosisRevision.notificationDate = Instant.now()
        diagnosisRevision.notificationStatus = NotificationStatus.NOTIFICATION_COMPLETED

        if (save)
            tmp = taskRepository.save(tmp, resourceBundle["log.diagnosisRevision.notificationCompleted", diagnosisRevision], tmp.patient)

        return tmp
    }

    /**
     * Generated or updates the default reportIntent report for a reportIntent
     */
    @Transactional
    open fun generateDefaultDiagnosisReport(task: Task, diagnosisRevision: DiagnosisRevision): Task {
        logger.debug("Generating new report")
        return DiagnosisReportUpdater().updateDiagnosisReportNoneBlocking(task, diagnosisRevision)
    }

    /**
     * Updates a reportIntent with out a reportIntent prototype
     */
    @Transactional
    open fun updateDiagnosisWithoutPrototype(task: Task, diagnosis: Diagnosis, diagnosisAsText: String,
                                             extendedDiagnosisText: String, malign: Boolean, icd10: String): Task {
        diagnosis.diagnosisPrototype = null
        return updateDiagnosis(task, diagnosis, diagnosisAsText, extendedDiagnosisText, malign, icd10)
    }

    /**
     * Updates a reportIntent with a diagnosispreset.
     */
    @Transactional
    open fun updateDiagnosisWithPrototype(task: Task, diagnosis: Diagnosis, preset: DiagnosisPreset): Task {
        logger.debug("Updating reportIntent with prototype")
        diagnosis.diagnosisPrototype = preset
        return updateDiagnosis(task, diagnosis, preset.diagnosis, preset.extendedDiagnosisText,
                preset.isMalign, preset.icd10)
    }

    /**
     * Updates a reportIntent with the given data. Also updates notification via letter
     */
    @Transactional
    open fun updateDiagnosis(task: Task, diagnosis: Diagnosis, diagnosisAsText: String, extendedDiagnosisText: String,
                             malign: Boolean, icd10: String): Task {
        var tmp = task
        logger.debug("Updating reportIntent to $diagnosisAsText")

        diagnosis.diagnosis = diagnosisAsText
        diagnosis.malign = malign
        diagnosis.icd10 = icd10

        // only setting reportIntent text if one sample and no text has been
        // added
        // jet
        if (diagnosis.parent!!.text.isEmpty()) {
            diagnosis.parent!!.text = extendedDiagnosisText
            logger.debug("Updating revision extended text")
        }

        // updating all contacts on reportIntent change, an determine if the
        // contact should receive a physical case report
        tmp = reportIntentService.updateReportIntentNotificationHistoryWithDiagnoses(tmp, true)
        tmp = taskRepository.save(tmp, resourceBundle["log.diagnosis.update", diagnosis, diagnosis.diagnosis])
        return task
    }

    /**
     * Renames all diagnoses
     */
    @Transactional
    open fun renameDiagnosisRevisions(task: Task, containers: List<CreateDiagnosisRevisionDialog.DiagnosisRevisionContainer>): Task {
        var task = task

        var changed = false
        for (diagnosisRevisionContainer in containers) {
            if (diagnosisRevisionContainer.newName != diagnosisRevisionContainer.name) {
                logger.debug("Updating revision name from  ${diagnosisRevisionContainer.name} to ${diagnosisRevisionContainer.name})")
                // updating name
                diagnosisRevisionContainer.name = diagnosisRevisionContainer.newName
                changed = true
            }
        }

        if (changed)
            task = taskRepository.save(task,
                    resourceBundle["log.diagnosisRevision.nameUpdate", task], task.patient)

        return task
    }

    /**
     * Copies the histological record
     */
    @Transactional
    open fun copyHistologicalRecord(diagnosis: Diagnosis, overwrite: Boolean): Task {

        val text = if (overwrite)
            diagnosis.diagnosisPrototype!!.extendedDiagnosisText
        else
            "${diagnosis.parent!!.text}\r\n ${diagnosis.diagnosisPrototype!!.extendedDiagnosisText}"

        diagnosis.parent!!.text = text

        return taskRepository.save<Task>(diagnosis.task, resourceBundle["log.diagnosisRevision.histologicalRecordCopy", diagnosis.diagnosis], diagnosis.patient)
    }

    companion object {
        /**
         * Returns the next diagnosisrevision where the completion date is 0, if no
         * reportIntent is found null is returned.
         */
        @JvmStatic
        fun getNextRevisionToApprove(task: Task): DiagnosisRevision? {
            return task.diagnosisRevisions.firstOrNull { !it.completed }
        }


        /**
         * Counts diagnoses which are not completed jet,
         */
        @JvmStatic
        fun countRevisionToApprove(task: Task): Int {
            return task.diagnosisRevisions.count { !it.completed }
        }
    }
}