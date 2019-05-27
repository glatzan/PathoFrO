package com.patho.main.service

import com.patho.main.common.ContactRole
import com.patho.main.config.PathoConfig
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.*
import com.patho.main.model.person.Organization
import com.patho.main.model.person.Person
import com.patho.main.repository.AssociatedContactRepository
import com.patho.main.repository.ReportIntentNotificationRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.util.exception.DuplicatedReportIntentException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import javax.persistence.Transient
import javax.transaction.Transactional

/**
 * ReportIntent 1 -> 4 ReportIntentNotification 1 -> n (diagnoses) DiagnosisHistoryRecord 1 -> n HistoryEntry
 */
@Service()
open class ReportIntentService @Autowired constructor(
        private val associatedContactRepository: AssociatedContactRepository,
        private val pathoConfig: PathoConfig,
        private val reportIntentNotificationRepository: ReportIntentNotificationRepository,
        private val taskRepository: TaskRepository) : AbstractService() {


    /**
     * Adds a new report intent to the task. Adding of a person twice with the same role will be prevented.
     */
    @Transactional
    open fun addReportIntent(task: Task, person: Person, role: ContactRole, defaultNotification: Boolean = false, deleteable: Boolean = true, save: Boolean = true): Pair<Task, ReportIntent> {
        return addReportIntent(task, ReportIntent(task, person, role, deleteable), defaultNotification, save)
    }

    /**
     * Adds a new report intent to the task. Adding of a person twice with the same role will be prevented.
     */
    @Transactional
    open fun addReportIntent(task: Task, reportIntent: ReportIntent, defaultNotification: Boolean = false, save: Boolean = true): Pair<Task, ReportIntent> {
        logger.debug("Add Report Intent for ${reportIntent.person?.getFullName()}")
        var reportIntent = reportIntent

        // checks if a report intent with the same person is present
        if (task.contacts.contains(reportIntent))
            throw DuplicatedReportIntentException()

        task.contacts.add(reportIntent)
        reportIntent.task = task


        // add notifications
        if (defaultNotification)
            updateReportIntentNotificationsWithRole(task, reportIntent, save = false)

        if (save)
            reportIntent = associatedContactRepository.save(reportIntent,
                    resourceBundle.get("log.contact.add", task, reportIntent),
                    reportIntent.task!!.patient)

        return Pair(task, reportIntent)
    }

    /**
     * Removes a report intent if no history is present, if history is present it will be deactivated
     */
    @Transactional
    open fun removeReportIntent(task: Task, reportIntent: ReportIntent): Task {
        if (isHistoryPresent(reportIntent)) {
            reportIntent.active = false
            associatedContactRepository.save(reportIntent, resourceBundle.get("log.reportIntent.deactivated", task, reportIntent.task!!.patient), reportIntent.task!!.patient)
        } else {
            task.contacts.remove(reportIntent)
            // only associatedContact has to be removed, is the mapping entity
            associatedContactRepository.delete(reportIntent,
                    resourceBundle.get("log.reportIntent.removed", task, reportIntent.task!!.patient),
                    reportIntent.task!!.patient)
        }
        return task
    }


    /**
     * Adds a new notification to a report intent
     */
    @Transactional
    open fun addReportIntentNotification(task: Task, reportIntent: ReportIntent, type: NotificationTyp, customAddress: String = "", save: Boolean = true): Pair<ReportIntent, ReportIntentNotification> {
        logger.debug("Adding notification of type $type")

        val reportIntentNotification = findReportIntentNotificationByType(reportIntent, type)
                ?: ReportIntentNotification(reportIntent, type, true)

        logger.debug("${reportIntentNotification.id}")

        reportIntentNotification.active = true
        reportIntentNotification.contactAddress = customAddress

        // updating the history
        updateReportIntentNotificationHistoryWithDiagnoses(task, reportIntentNotification)

        if (!reportIntent.notifications.contains(reportIntentNotification))
            reportIntent.notifications.add(reportIntentNotification)

        // saves the notification
        if (save) {
            val tmp = associatedContactRepository
                    .save(reportIntent,
                            resourceBundle.get("log.reportIntent.notification.add", reportIntent.task,
                                    reportIntent, type.toString()),
                            reportIntent.task!!.patient)

            return Pair(tmp, findReportIntentNotificationByType(tmp, type) ?: reportIntentNotification);
        }

        // returns an unsaved notification
        return Pair(reportIntent, reportIntentNotification)
    }

    /**
     * Removes a notification path from the report intent
     */
    @Transactional
    open fun removeReportIntentNotification(task: Task, reportIntent: ReportIntent, type: NotificationTyp): Pair<Task, ReportIntent> {
        val tmp = findReportIntentNotificationByType(reportIntent, type)
        if (tmp != null)
            return removeReportIntentNotification(task, reportIntent, tmp)
        return Pair(task, reportIntent)
    }

    /**
     * Removes a notification path from the report intent
     */
    @Transactional
    open fun removeReportIntentNotification(task: Task, reportIntent: ReportIntent, reportIntentNotification: ReportIntentNotification): Pair<Task, ReportIntent> {
        if (isHistoryPresent(reportIntentNotification)) {
            reportIntentNotification.active = false
            return Pair(task, reportIntent)
        } else {
            reportIntent.notifications.remove(reportIntentNotification)

            // saving for log
            var reportIntent = associatedContactRepository.save(reportIntent,
                    resourceBundle.get("log.reportIntent.notification.removed", reportIntent.task,
                            reportIntent.toString(), reportIntentNotification.notificationTyp.toString()),
                    reportIntent.task!!.patient)


            // only remove from array, and deleting the entity only (no saving
            // of contact necessary because mapped within notification)
            reportIntentNotificationRepository.delete(reportIntentNotification)
            return Pair(task, reportIntent)
        }
    }

    /**
     * Enables or disables a notification of a report intent
     */
    open fun toggleReportIntentNotificationActiveStatus(task: Task, reportIntentNotification: ReportIntentNotification, enable: Boolean): Pair<Task, ReportIntent> {
        val reportIntent = reportIntentNotification?.contact ?: throw IllegalStateException()
        return toggleReportIntentNotificationActiveStatus(task, reportIntent, reportIntentNotification, enable)
    }

    /**
     * Enables or disables a notification of a report intent
     */
    open fun toggleReportIntentNotificationActiveStatus(task: Task, reportIntent: ReportIntent, reportIntentNotification: ReportIntentNotification, enable: Boolean): Pair<Task, ReportIntent> {
        reportIntentNotification.active = enable

        // saving for log
        var reportIntent = associatedContactRepository.save(reportIntent,
                resourceBundle.get(if (enable) "log.reportIntent.notification.enabled" else "log.reportIntent.notification.disabled", reportIntent.task,
                        reportIntent.toString(), reportIntentNotification.notificationTyp.toString()),
                reportIntent.task!!.patient)

        return Pair(task, reportIntent)
    }

    /**
     * Updates the notifications depending on the reportIntent role
     */
    @Transactional
    open fun updateReportIntentNotificationsWithRole(task: Task): Task {
        task.contacts.forEach { p -> updateReportIntentNotificationsWithRole(task, p) }
        return task
    }

    /**
     * Updates the notifications depending on the reportIntent role
     */
    @Transactional
    open fun updateReportIntentNotificationsWithRole(task: Task, reportIntent: ReportIntent, save: Boolean = true): Pair<Task, ReportIntent> {
        logger.debug("Updating default Notifications")

        // do nothing if there are some notifications
        if (isHistoryPresent(reportIntent))
            return Pair(task, reportIntent)

        val notifications: List<NotificationTyp> = pathoConfig.defaultNotification.getDefaultNotificationForRole(reportIntent.role)

        notifications.forEach { p -> addReportIntentNotification(task, reportIntent, p, save = false) }

        return updateReportIntentNotificationsWithDiagnosisPresets(task, reportIntent, save = save)
    }

    /**
     * Checks if the reportIntent has a role to which a letter should be send
     */
    @Transactional
    open fun updateReportIntentNotificationsWithDiagnosisPresets(task: Task, reportIntent: ReportIntent, save: Boolean = true): Pair<Task, ReportIntent> {
        logger.debug("Updating with diagnosispresets")
        val rolesToUpdate = mutableListOf<ContactRole>()

        // collecting roles for which a report should be send by letter
        for (diagnosisRevision in task.diagnosisRevisions)
            for (diagnosis in diagnosisRevision.diagnoses) {
                val tmp = diagnosis.diagnosisPrototype?.diagnosisReportAsLetter
                if (tmp != null) rolesToUpdate.addAll(tmp)
            }

        // checking if contact is within the send letter to roles
        if (isContactRolePresent(reportIntent, rolesToUpdate))
        // adding notification and return
            addReportIntentNotification(task, reportIntent, NotificationTyp.LETTER, save = false)


        if (save) {
            // TODO: save
        }

        return Pair(task, reportIntent)
    }

    /**
     * Adds a diagnosisHistoryRecord to a notification
     */
    @Transactional
    open fun addDiagnosisHistoryRecord(task: Task, reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision, save: Boolean = true): Pair<ReportIntentNotification, DiagnosisHistoryRecord> {
        logger.debug("Adding history for reportIntent $diagnosisRevision")

        val result = DiagnosisHistoryRecord(diagnosisRevision)
        reportIntentNotification.history.add(result)

        if (save) {
            var tmp = reportIntentNotificationRepository.save(reportIntentNotification, resourceBundle.get("log.reportIntent.notification.record.add", task, diagnosisRevision, reportIntentNotification.contact?.person), task!!.patient)
            return Pair(tmp, result)
        }

        return Pair(reportIntentNotification, result)
    }

    /**
     * Removes a diagnosisHistoryRecord from a notification
     */
    @Transactional
    open fun removeDiagnosisHistoryRecord(reportIntentNotification: ReportIntentNotification, diagnosisHistoryRecord: DiagnosisHistoryRecord) {
        logger.debug("Removing unused history")
        reportIntentNotification.history.remove(diagnosisHistoryRecord)
    }

    /**
     * Updates the ReportIntentNotification history to match the current diagnoses
     */
    @Transactional
    open fun updateReportIntentNotificationHistoryWithDiagnoses(task: Task, save: Boolean = true): Task {
        for (notification in task.contacts) {
            updateReportIntentNotificationHistoryWithDiagnoses(task, notification, save = false)
        }

        if (save)
            return taskRepository.save(task, resourceBundle.get("log.reportIntent.notification.updated", task), task!!.patient)

        return task;
    }

    /**
     * Updates the ReportIntentNotification history to match the current diagnoses
     */
    @Transactional
    open fun updateReportIntentNotificationHistoryWithDiagnoses(task: Task, reportIntent: ReportIntent, save: Boolean = true): Pair<Task, ReportIntent> {
        for (notification in reportIntent.notifications) {
            updateReportIntentNotificationHistoryWithDiagnoses(task, notification, save = false)
        }

        if (save) {
            var tmp = associatedContactRepository.save(reportIntent, resourceBundle.get("log.reportIntent.notification.updated", task), task!!.patient)
            return Pair(task, tmp)
        }

        return Pair(task, reportIntent)
    }

    /**
     * Updates the ReportIntentNotification history to match the current diagnoses
     */
    @Transactional
    open fun updateReportIntentNotificationHistoryWithDiagnoses(task: Task, reportIntentNotification: ReportIntentNotification, save: Boolean = true): Pair<Task, ReportIntentNotification> {
        logger.debug("Updating History with diagnoses")

        val foundRevisions = mutableListOf<DiagnosisRevision>()
        var toRemoveHistory = mutableListOf<DiagnosisHistoryRecord>()

        for (diagnosisHistoryRecord in reportIntentNotification.history) {
            val diagnosisRevision = task.diagnosisRevisions.singleOrNull { p -> p.id == diagnosisHistoryRecord.diagnosisID }

            // search if a corresponding reportIntent exists
            if (diagnosisRevision != null) {
                foundRevisions.add(diagnosisRevision)
            } else {
                // if a notification was performed do not remove!
                if (isHistoryPresent(diagnosisHistoryRecord))
                    diagnosisHistoryRecord.diagnosisPresent = false
                else
                    toRemoveHistory.add(diagnosisHistoryRecord)
            }
        }

        // removing
        toRemoveHistory.forEach { p -> removeDiagnosisHistoryRecord(reportIntentNotification, p) }

        val allRevisions = mutableListOf<DiagnosisRevision>(*task.diagnosisRevisions.toTypedArray())
        allRevisions.removeAll(foundRevisions)

        // adding new diagnoses
        for (diagnosisRevision in allRevisions) {
            addDiagnosisHistoryRecord(task, reportIntentNotification, diagnosisRevision, false)
        }

        if (save) {
            var tmp = reportIntentNotificationRepository.save(reportIntentNotification, resourceBundle.get("log.reportIntent.notification.updated", task), task!!.patient)
            return Pair(task, tmp)
        }

        return Pair(task, reportIntentNotification)
    }

    /**
     * Adds history data to an reportIntentNotification. If the reportIntentNotification is not present, it will be created as well.
     */
    @Transactional
    open fun addHistoryEntryAndReportIntentNotification(task: Task, reportIntent: ReportIntent, type: NotificationTyp, diagnosisRevision: DiagnosisRevision, actionDate: Instant = Instant.now(), failed: Boolean = false, commentary: String = "", save: Boolean = true): Pair<ReportIntent, HistoryEntry> {
        var reportIntentNotification = findReportIntentNotificationByType(reportIntent, type)

        if (reportIntentNotification == null)
            reportIntentNotification = addReportIntentNotification(task, reportIntent, type, "", save = false).second

        var historyEntry = addHistoryEntry(task, reportIntentNotification, diagnosisRevision, actionDate, failed, commentary, false)

        if (save) {
            val tmp = associatedContactRepository
                    .save(reportIntent,
                            resourceBundle.get("log.reportIntent.notification.historyEntryAdded", reportIntent.task,
                                    reportIntent, type.toString()),
                            reportIntent.task!!.patient)
            return Pair(tmp, historyEntry.second);
        }

        return Pair(reportIntent, historyEntry.second);
    }

    /**
     * Adds a history record to a notification
     */
    @Transactional
    open fun addHistoryEntry(task: Task, reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision, actionDate: Instant = Instant.now(), failed: Boolean = false, commentary: String = "", save: Boolean = true): Pair<ReportIntentNotification, HistoryEntry> {
        val reportData = HistoryEntry()
        reportData.commentary = commentary
        reportData.actionDate = actionDate
        reportData.contactAddress = reportIntentNotification.contactAddress ?: ""
        reportData.failed = failed

        (findDiagnosisHistoryRecordByDiagnosis(reportIntentNotification, diagnosisRevision)
                ?: addDiagnosisHistoryRecord(task, reportIntentNotification, diagnosisRevision, false).second).data.add(reportData)

        if (save) {
            var tmp =  reportIntentNotificationRepository.save(reportIntentNotification, resourceBundle.get("log.reportIntent.notification.diagnosisRecord.record.add", task, reportIntentNotification.contact?.person), task!!.patient)
            return Pair(tmp, reportData)
        }

        return Pair(reportIntentNotification, reportData)
    }


    /**
     * Checks if a notification was performed
     */
    @Transactional
    open fun isHistoryPresent(reportIntent: ReportIntent): Boolean {
        return reportIntent.notifications.any { p -> isHistoryPresent(p) }
    }

    /**
     * Checks if a notification was performed
     */
    @Transactional
    open fun isHistoryPresent(reportIntentNotification: ReportIntentNotification): Boolean {
        return reportIntentNotification.history.any { p -> isHistoryPresent(p) }
    }

    /**
     * Checks if a notification was performed
     */
    @Transactional
    open fun isHistoryPresent(diagnosisHistoryRecord: DiagnosisHistoryRecord): Boolean {
        return diagnosisHistoryRecord.data.isNotEmpty()
    }

    /**
     * Search for a notification with the given type. If not found null is returned.
     */
    @Transactional
    open fun findReportIntentNotificationByType(reportIntent: ReportIntent, type: NotificationTyp): ReportIntentNotification? {
        return reportIntent.notifications.lastOrNull { p -> p.notificationTyp == type }
    }

    /**
     * Searches for a history record with the given reportIntent id, returns last element
     */
    @Transactional
    open fun findDiagnosisHistoryRecordByDiagnosis(reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision): DiagnosisHistoryRecord? {
        return reportIntentNotification.history.lastOrNull { p -> p.diagnosisID == diagnosisRevision.id }
    }

    /**
     * Searches for all history records with the given reportIntent id
     */
    @Transactional
    open fun findCompletedDiagnosisHistoryRecordByDiagnosis(reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision): List<DiagnosisHistoryRecord> {
        return reportIntentNotification.history.filter { p -> p.diagnosisID == diagnosisRevision.id }
    }


    /**
     * Checks if the ReportIntent has one of the given contact roles
     */
    @Transactional
    open fun isContactRolePresent(reportIntent: ReportIntent, contactRoles: MutableList<ContactRole>): Boolean {
        return contactRoles.any { p -> reportIntent.role == p }
    }


    /**
     * Checks if all notifications are performed
     */
    @Transient
    open fun isNotificationPerformed(reportIntent: ReportIntent): Boolean {
        return if(reportIntent.notifications.isNotEmpty()) reportIntent.notifications.all { p -> isNotificationPerformed(p) } else false
    }

    /**
     * Checks if all notifications are performed
     */
    @Transient
    open fun isNotificationPerformed(reportIntentNotification: ReportIntentNotification): Boolean {
        return if(reportIntentNotification.history.isNotEmpty()) reportIntentNotification.history.all { p -> isNotificationPerformed(p) } else false
    }

    /**
     * Checks if all notifications are performed
     */
    open fun isNotificationPerformed(diagnosisHistoryRecord: DiagnosisHistoryRecord): Boolean {
        return !(diagnosisHistoryRecord.data.lastOrNull()?.failed ?: true) && diagnosisHistoryRecord.data.isNotEmpty()
    }

    /**
     * Checks if the notifications of the given type were performed
     */
    open fun isNotificationPerformed(reportIntent: ReportIntent, type: NotificationTyp): Boolean {
        val tmp = findReportIntentNotificationByType(reportIntent, type)
        return if (tmp != null) isNotificationPerformed(tmp) else false
    }

    /**
     * Generates a address for a report intent, uses the default address if set
     */
    open fun generateAddress(reportIntent: ReportIntent): String {
        return generateAddress(reportIntent, reportIntent.person?.defaultAddress)
    }

    /**
     * Generates a address for the given report intent
     */
    open fun generateAddress(reportIntent: ReportIntent, organization: Organization? = null): String {
        val buffer = StringBuffer()

        buffer.append((reportIntent.person?.getFullName() ?: "") + "\r\n")

        val addition1 = if (organization != null) organization?.contact?.addressadditon else reportIntent.person?.contact?.addressadditon
        val addition2 = if (organization != null) organization?.contact?.addressadditon2 else reportIntent.person?.contact?.addressadditon2
        val street = if (organization != null) organization?.contact?.street else reportIntent.person?.contact?.street
        val postcode = if (organization != null) organization?.contact?.postcode else reportIntent.person?.contact?.postcode
        val town = if (organization != null) organization?.contact?.town else reportIntent.person?.contact?.town

        if (organization != null)
            buffer.append(organization.name + "\r\n")

        if (addition1.isNullOrEmpty()) buffer.append("$addition1\r\n")
        if (addition2.isNullOrEmpty()) buffer.append("$addition2\r\n")
        if (street.isNullOrEmpty()) buffer.append("$street\r\n")
        if (postcode.isNullOrEmpty()) buffer.append("$postcode\r\n")
        if (town.isNullOrEmpty()) buffer.append("$town\r\n")

        return buffer.toString()
    }

}