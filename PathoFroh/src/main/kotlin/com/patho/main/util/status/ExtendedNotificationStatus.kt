package com.patho.main.util.status

import com.patho.main.common.DiagnosisRevisionType
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.DiagnosisHistoryRecord
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.status.reportIntent.ReportIntentBearer

class ExtendedNotificationStatus(val task: Task) {

    /**
     * Contains all diagnoses, even diagnoses which had been deleted
     */
    private val allDiagnoses = findAllDiagnoses(task.diagnosisRevisions, task.contacts)

    val diagnosisNotificationStatus = DiagnosisNotificationStatus(task, allDiagnoses)

    val reportNotificationIntentStatus = ReportNotificationIntentStatus(task, allDiagnoses)
    /**
     * <pre>
     *
     * </pre>
     */
    class ReportNotificationIntentStatus(task: Task, allDiagnoses: Set<DiagnosisRevision>) {

        val reportIntents = task.contacts.map { ReportIntentStatus(it, task) }

        class ReportIntentStatus(val reportIntent: ReportIntent, task: Task) {

            val diagnosis = mutableListOf<ReportIntentBearer.DiagnosisBearer>()

            class DiagnosisRevisionStatus(val diagnosisID: Long, task: Task) {

                val reportIntentNotificationBearers = mutableListOf<ReportIntentNotificationBearer>()

                val diagnosisRevision = (task.diagnosisRevisions.firstOrNull { p -> p.id == diagnosisID })

                val diagnosisName: String = diagnosisRevision?.name
                        ?: SpringContextBridge.services().resourceBundle.get("Deleted")

                open class ReportIntentNotificationBearer(var type: NotificationTyp, val diagnosisHistoryRecord: DiagnosisHistoryRecord) {
                    val success = SpringContextBridge.services().reportIntentService.isNotificationPerformed(diagnosisHistoryRecord)
                    val successes = diagnosisHistoryRecord.data.count { p -> !p.failed }
                    val failedAttempts = diagnosisHistoryRecord.data.count { p -> p.failed }
                    val attempts = diagnosisHistoryRecord.data.size
                    val reportData = diagnosisHistoryRecord.data.toList()

                }
            }
        }
    }

    /**
     * <pre>
     * Class for sorting the notification status by diagnosis:
     *  // list of all diagnoses
     *  diagnosis (List, DiagnosisRevisionStatus bearer for DiagnosisRevision) ->
     *         name
     *         ....
     *         // list of all reportIntents that are associated with the diagnosis
     *         reportIntents (List, ReportIntentStatus bearer for ReportIntent)
     *              isActive
     *              ....
     *              // list of a notification type + history record which is associated with the diagnosis
     *              notifications (List, NotificationTypeAndHistory bearer for  NotificationTyp + DiagnosisHistoryRecord associated with the diagnosis)
     *                  history (DiagnosisHistoryRecord)
     *                  notificationTyp (NotificationTyp)
     *
     *
     * </pre>
     */
    class DiagnosisNotificationStatus(task: Task, allDiagnoses: Set<DiagnosisRevision>) {

        /**
         * List of all diagnoses, including deleted ones. (The deleted diagnoses are extracted from the notification history
         */
        val diagnoses = allDiagnoses.map { it ->
            DiagnosisRevisionStatus(it, task.contacts)
        }

        /**
         * This class contains the status of one diagnosis and of the associated reportIntents
         */
        class DiagnosisRevisionStatus(val diagnosisRevision: DiagnosisRevision, reportIntents: Set<ReportIntent>) {
            /**
             * Diagnosis Name
             */
            val name: String = diagnosisRevision.name

            /**
             * All associated reportIntents
             */
            val reportIntents: List<ReportIntentStatus> = findReportIntents(reportIntents)

            /**
             * All pending report intents
             */
            val activeReportIntent: List<ReportIntentStatus>
                get() = reportIntents.filter { it.isActive }

            /**
             * Returns true if there
             */
            val isActiveReportIntent
                get() = reportIntents.any { it.isActive }

            /**
             * This class contains the associated reportIntent and also the associated history records
             */
            class ReportIntentStatus(val reportIntent: ReportIntent, notificationHistoryRecords: List<NotificationTypeAndHistory>) {
                val isActive = reportIntent.active

                val mailRecord = notificationHistoryRecords.firstOrNull { p -> p.notificationTyp == NotificationTyp.EMAIL }
                val isMailRecord = mailRecord != null
                val isMailActive = isActive && mailRecord?.active ?: false
                val mailPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(mailRecord?.history
                        ?: DiagnosisHistoryRecord())

                val faxRecord = notificationHistoryRecords.firstOrNull { p -> p.notificationTyp == NotificationTyp.FAX }
                val isFaxRecord = faxRecord != null
                val isFaxActive = isActive && faxRecord?.active ?: false
                val faxPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(faxRecord?.history
                        ?: DiagnosisHistoryRecord())

                val letterRecord = notificationHistoryRecords.firstOrNull { p -> p.notificationTyp == NotificationTyp.LETTER }
                val isLetterRecord = letterRecord != null
                val isLetterActive = isActive && letterRecord?.active ?: false
                val letterPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(letterRecord?.history
                        ?: DiagnosisHistoryRecord())

                val phoneRecord = notificationHistoryRecords.firstOrNull { p -> p.notificationTyp == NotificationTyp.PHONE }
                val isPhoneRecord = phoneRecord != null
                val isPhoneActive = isActive && phoneRecord?.active ?: false
                val phonePerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(phoneRecord?.history
                        ?: DiagnosisHistoryRecord())
            }

            /**
             * Searches for reportIntents which are associated with the current diagnosis. If matches are found
             * a new ReportIntentStatus will be created containing the history for all notification types
             */
            private fun findReportIntents(reportIntents: Set<ReportIntent>): List<ReportIntentStatus> {
                val reportIntentStatus = mutableListOf<ReportIntentStatus>()
                val notificationHistoryRecord = mutableListOf<NotificationTypeAndHistory>()

                reportIntents.forEach { r ->
                    r.notifications.forEach { n ->
                        val lastHistory = n.history.lastOrNull { h -> h.diagnosisID == this.diagnosisRevision.id }
                        if (lastHistory != null) {
                            notificationHistoryRecord.add(NotificationTypeAndHistory(lastHistory, n.notificationTyp, n.active))
                        }
                    }

                    if (notificationHistoryRecord.size != 0) {
                        reportIntentStatus.add(ReportIntentStatus(r, notificationHistoryRecord))
                        notificationHistoryRecord.clear()
                    }

                }

                return reportIntentStatus
            }
        }
    }

    /**
     * Class for history record and notification type
     */
    class NotificationTypeAndHistory(val history: DiagnosisHistoryRecord, val notificationTyp: NotificationTyp, val active: Boolean)
//TODO: merge
//open class ReportIntentNotificationBearer(var type: NotificationTyp, val diagnosisHistoryRecord: DiagnosisHistoryRecord) {
//    val success = SpringContextBridge.services().reportIntentService.isNotificationPerformed(diagnosisHistoryRecord)
//    val successes = diagnosisHistoryRecord.data.count { p -> !p.failed }
//    val failedAttempts = diagnosisHistoryRecord.data.count { p -> p.failed }
//    val attempts = diagnosisHistoryRecord.data.size
//    val reportData = diagnosisHistoryRecord.data.toList()
//
//}


    /**
     * Returns a list with all ids of current and deleted diagnosis revisions. The current ids will be taken for a
     * list of diagnosisRevisions, the deleted ids will be taken from the history of the reportIntents.
     */
    private fun findAllDiagnoses(diagnosisRevisions: Set<DiagnosisRevision>, reportIntents: Set<ReportIntent>): Set<DiagnosisRevision> {
        val revisions = hashSetOf<DiagnosisRevision>()
        revisions.addAll(diagnosisRevisions)
        reportIntents.forEach { p ->
            p.notifications.forEach { n ->
                n.history.forEach { h ->
                    if (!revisions.any { it.id == h.diagnosisID }) {
                        val newRev = DiagnosisRevision(SpringContextBridge.services().resourceBundle.get("Deleted"), DiagnosisRevisionType.TMP)
                        newRev.id = h.diagnosisID
                        revisions.add(newRev)
                    }
                }
            }
        }
        return revisions
    }
}