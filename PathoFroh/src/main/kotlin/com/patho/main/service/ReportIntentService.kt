package com.patho.main.service

import com.patho.main.common.ContactRole
import com.patho.main.config.PathoConfig
import com.patho.main.model.person.Organization
import com.patho.main.model.person.Person
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportHistoryRecord
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.repository.AssociatedContactNotificationRepository
import com.patho.main.repository.AssociatedContactRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import javax.persistence.Transient
import javax.transaction.Transactional

/**
 * ReportIntent 1 -> 4 ReportIntentNotification 1 -> n (diagnoses) ReportHistoryRecord 1 -> n ReportData
 */
@Service()
open class ReportIntentService @Autowired constructor(
        private val associatedContactRepository: AssociatedContactRepository,
        private val pathoConfig: PathoConfig,
        private val associatedContactNotificationRepository: AssociatedContactNotificationRepository) : AbstractService() {


    /**
     * Adds a new report intent to the task. Adding of a person twice with the same role will be prevented.
     */
    @Transactional
    open fun addReportIntent(task: Task, person: Person, role: ContactRole): Pair<Task, ReportIntent> {
        return addReportIntent(task, ReportIntent(task, person, role))
    }

    /**
     * Adds a new report intent to the task. Adding of a person twice with the same role will be prevented.
     */
    @Transactional
    open fun addReportIntent(task: Task, reportIntent: ReportIntent, defaultNotification: Boolean = false, save: Boolean = true): Pair<Task, ReportIntent> {
        var reportIntent = reportIntent

        // checks if a report intent with the same person is present
        if (task.contacts.contains(reportIntent))
            throw IllegalArgumentException("Already in list")

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
    open fun addReportIntentNotification(task: Task, reportIntent: ReportIntent, type: ReportIntentNotification.NotificationTyp, customAddress: String = "", save: Boolean = true): Pair<ReportIntent, ReportIntentNotification> {
        logger.debug("Adding notification of type $type")

        val reportIntentNotification = findReportIntentNotificationByType(reportIntent, type)
                ?: ReportIntentNotification(reportIntent, type, true)

        reportIntentNotification.active = true
        reportIntentNotification.contactAddress = customAddress

        // updating the history
        updateReportIntentNotificationHistoryWithDiagnoses(task, reportIntentNotification)

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
    open fun removeReportIntentNotification(task: Task, reportIntent: ReportIntent, type: ReportIntentNotification.NotificationTyp): Pair<Task, ReportIntent> {
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
            associatedContactNotificationRepository.delete(reportIntentNotification)
            return Pair(task, reportIntent)
        }
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
        logger.debug("Updating Notifications")

        // do nothing if there are some notifications
        if (isHistoryPresent(reportIntent))
            return Pair(task, reportIntent)


        val notifications: List<ReportIntentNotification.NotificationTyp> = pathoConfig.defaultNotification.getDefaultNotificationForRole(reportIntent.role)

        notifications.forEach { p -> addReportIntentNotification(task, reportIntent, p, save = false) }

        return updateReportIntentNotificationsWithDiagnosisPresets(task, reportIntent, save = save)
    }

    /**
     * Checks if the reportIntent has a role to which a letter should be send
     */
    @Transactional
    open fun updateReportIntentNotificationsWithDiagnosisPresets(task: Task, reportIntent: ReportIntent, save: Boolean = true): Pair<Task, ReportIntent> {
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
            addReportIntentNotification(task, reportIntent, ReportIntentNotification.NotificationTyp.LETTER, save = false)


        if (save) {
            // TODO: save
        }

        return Pair(task, reportIntent)
    }

    /**
     * Adds a reportHistoryRecord to a notification
     */
    @Transactional
    open fun addReportHistoryRecord(reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision): ReportHistoryRecord {
        val result = ReportHistoryRecord(diagnosisRevision)
        reportIntentNotification.history.add(result)
        return result
    }

    /**
     * Removes a reportHistoryRecord from a notification
     */
    @Transactional
    open fun removeReportHistoryRecord(reportIntentNotification: ReportIntentNotification, reportHistoryRecord: ReportHistoryRecord) {
        reportIntentNotification.history.remove(reportHistoryRecord)
    }

    /**
     * Updates the ReportIntentNotification history to match the current diagnoses
     */
    @Transactional
    open fun updateReportIntentNotificationHistoryWithDiagnoses(task: Task, reportIntent: ReportIntent) {
        for (notification in reportIntent.notifications) {
            updateReportIntentNotificationHistoryWithDiagnoses(task, notification)
        }
    }

    /**
     * Updates the ReportIntentNotification history to match the current diagnoses
     */
    @Transactional
    open fun updateReportIntentNotificationHistoryWithDiagnoses(task: Task, reportIntentNotification: ReportIntentNotification, save: Boolean = true) {

        val foundRevisions = mutableListOf<DiagnosisRevision>()

        for (reportHistoryRecord in reportIntentNotification.history) {
            val diagnosisRevision = task.diagnosisRevisions.singleOrNull { p -> p.id == reportIntentNotification.id }

            // search if a corresponding diagnosis exists
            if (diagnosisRevision != null) {
                foundRevisions.add(diagnosisRevision)
            } else {
                // if a notification was performed do not remove!
                if (isHistoryPresent(reportHistoryRecord))
                    reportHistoryRecord.diagnosisPresent = false
                else
                    removeReportHistoryRecord(reportIntentNotification, reportHistoryRecord)
            }
        }

        val allRevisions = mutableListOf<DiagnosisRevision>(*task.diagnosisRevisions.toTypedArray())
        allRevisions.removeAll(foundRevisions)

        // adding new diagnoses
        for (diagnosisRevision in allRevisions) {
            addReportHistoryRecord(reportIntentNotification, diagnosisRevision)
        }

        if (save) {
            // TODO: save
        }
    }

    /**
     * Adds a history record to a notification
     */
    @Transactional
    open fun addNotificationHistoryData(reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision, actionDate: Instant, failed: Boolean): ReportHistoryRecord.ReportData {
        val reportData = ReportHistoryRecord.ReportData()
        reportData.actionDate = actionDate
        reportData.contactAddress = reportIntentNotification.contactAddress ?: ""
        reportData.failed = failed
        (findReportHistoryRecordByDiagnosis(reportIntentNotification, diagnosisRevision)
                ?: addReportHistoryRecord(reportIntentNotification, diagnosisRevision)).data.add(reportData)
        return reportData
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
    open fun isHistoryPresent(reportHistoryRecord: ReportHistoryRecord): Boolean {
        return reportHistoryRecord.data.isNotEmpty()
    }

    /**
     * Search for a notification with the given type. If not found null is returned.
     */
    @Transactional
    open fun findReportIntentNotificationByType(reportIntent: ReportIntent, type: ReportIntentNotification.NotificationTyp): ReportIntentNotification? {
        return reportIntent.notifications.lastOrNull { p -> p.notificationTyp == type }
    }

    /**
     * Searches for a history record with the given diagnosis id, returns last element
     */
    @Transactional
    open fun findReportHistoryRecordByDiagnosis(reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision): ReportHistoryRecord? {
        return reportIntentNotification.history.lastOrNull { p -> p.diagnosisID == diagnosisRevision.id }
    }

    /**
     * Searches for all history records with the given diagnosis id
     */
    @Transactional
    open fun findCompleteReportHistoryRecordByDiagnosis(reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision): List<ReportHistoryRecord> {
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
        return reportIntent.notifications.all { p -> isNotificationPerformed(p) }
    }

    /**
     * Checks if all notifications are performed
     */
    @Transient
    open fun isNotificationPerformed(reportIntentNotification: ReportIntentNotification): Boolean {
        return reportIntentNotification.history.all { p -> isNotificationPerformed(p) }
    }

    /**
     * Checks if all notifications are performed
     */
    open fun isNotificationPerformed(reportHistoryRecord: ReportHistoryRecord): Boolean {
        return reportHistoryRecord.data.any { p -> !p.failed } && reportHistoryRecord.data.isNotEmpty()
    }

    /**
     * Checks if the notifications of the given type were performed
     */
    open fun isNotificationPerformed(reportIntent: ReportIntent, type: ReportIntentNotification.NotificationTyp): Boolean {
        val tmp = findReportIntentNotificationByType(reportIntent, type)
        return if (tmp != null) isNotificationPerformed(tmp) else false
    }

    /**
     * Generates a adress for the given report intent
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