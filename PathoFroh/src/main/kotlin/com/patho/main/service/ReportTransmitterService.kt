package com.patho.main.service

import com.patho.main.common.ContactRole
import com.patho.main.model.Person
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportHistoryRecord
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.patient.notification.ReportIntentNotification
import com.patho.main.repository.AssociatedContactRepository
import org.apache.velocity.app.event.ReferenceInsertionEventHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service()
open class ReportTransmitterService @Autowired constructor(
        private val associatedContactRepository: AssociatedContactRepository) : AbstractService() {


    /**
     * Adds a new report intent to the task. Adding of a person twice with the same role will be prevented.
     */
    open fun addReportIntent(task: Task, person: Person, role: ContactRole): Pair<Task, ReportIntent> {
        return addReportIntent(task, ReportIntent(task, person, role))
    }

    /**
     * Adds a new report intent to the task. Adding of a person twice with the same role will be prevented.
     */
    open fun addReportIntent(task: Task, reportIntent: ReportIntent): Pair<Task, ReportIntent> {
        // checks if a report intent with the same person is present
        if (task.contacts.contains(reportIntent))
            throw IllegalArgumentException("Already in list")

        task.contacts.add(reportIntent)
        reportIntent.task = task

        return Pair(task, associatedContactRepository.save(reportIntent,
                resourceBundle.get("log.contact.add", task, reportIntent),
                reportIntent.task!!.patient))
    }

    /**
     * Removes a report intent if no history is present, if history is present it will be deactivated
     */
    @Transactional
    open fun removeReportIntent(task: Task, reportIntent: ReportIntent) {
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
    }


    /**
     * Adds a new notification to a report intent
     */
    open fun addReportIntentNotification(task: Task, reportIntent: ReportIntent, type: ReportIntentNotification.NotificationTyp, customAddress: String = "", save: Boolean = true): Pair<ReportIntent, ReportIntentNotification> {
        logger.debug("Adding notification of type $type")

        val reportIntentNotification = findReportIntentNotificationByType(reportIntent, type)
                ?: ReportIntentNotification(reportIntent, type, true)

        reportIntentNotification.active = true
        reportIntentNotification.contactAddress = customAddress

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

    open fun updateReportIntentWithDiagnoses(task: Task, reportIntent: ReportIntent) {
        for (notification in reportIntent.notifications) {
            updateReportIntentNotificationWithDiagnoses(task, notification)
        }
    }

    open fun updateReportIntentNotificationWithDiagnoses(task: Task, reportIntentNotification: ReportIntentNotification) {

        for(reportHistoryRecord in reportIntentNotification.history){
            task.diagnosisRevisions.firstOrNull { p -> p.id == reportIntentNotification.id }

            reportHistoryRecord.diagnosisID
        }

        for (diagnosisRevision in task.diagnosisRevisions) {
            val record: ReportHistoryRecord? = findReportHistoryRecordByDiagnosis(reportIntentNotification, diagnosisRevision)

            if (record == null) reportIntentNotification.history.add(ReportHistoryRecord(diagnosisRevision))
        }
    }


    open fun updateReportIntentNotificationsWithRole(task: Task) {
        logger.debug("Updating Notifications")
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
    open fun findReportIntentNotificationByType(reportIntent: ReportIntent, type: ReportIntentNotification.NotificationTyp): ReportIntentNotification? {
        return reportIntent.notifications.lastOrNull { p -> p.notificationTyp == type }
    }

    /**
     * Search fo a history record with the given diagnosis id
     */
    open fun findReportHistoryRecordByDiagnosis(reportIntentNotification: ReportIntentNotification, diagnosisRevision: DiagnosisRevision): ReportHistoryRecord? {
        return reportIntentNotification.history.lastOrNull { p -> p.diagnosisID == diagnosisRevision.id }
    }


    /**
     * Returns all notification records for a diagnosis
     */
    open fun getReportHistoryForDiagnosis(reportIntent: ReportIntent, diagnosis: DiagnosisRevision): ReportHistoryJson.HistoryRecord {
        return reportIntent.history.records.filter { p -> p.diagnosisID == diagnosis.id }.firstOrNull()
                ?: ReportHistoryJson.HistoryRecord(diagnosis)
    }

    /**
     * Returns report data for a specific type and diagnosis
     */
    open fun getReportDataForDiagnosisAndType(reportIntent: ReportIntent, diagnosis: DiagnosisRevision, type: ReportIntentNotification.NotificationTyp): List<ReportHistoryJson.HistoryRecord.ReportData> {
        return getReportDataForType(getReportHistoryForDiagnosis(reportIntent, diagnosis), type)
    }

    /**
     * Returns the reportdata of a specific type
     */
    open fun getReportDataForType(historyRecord: ReportHistoryJson.HistoryRecord, type: ReportIntentNotification.NotificationTyp): List<ReportHistoryJson.HistoryRecord.ReportData> {
        return historyRecord.data.filter { p -> p.type == type }
    }

}