package com.patho.main.util.status

import com.patho.main.common.DiagnosisRevisionType
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.DiagnosisHistoryRecord
import com.patho.main.model.patient.notification.NotificationTyp
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.service.impl.SpringContextBridge

class ExtendedNotificationStatus(val task: Task) {

    /**
     * Contains all diagnoses, even diagnoses which had been deleted
     */
    private val allDiagnoses = findAllDiagnoses(task.diagnosisRevisions, task.contacts)

    /**
     * List of notification statuses sorted by diagnoses
     */
    val diagnosisNotificationStatus = DiagnosisNotificationStatus(task, allDiagnoses)

    /**
     * List of notification statuses sorted by reportIntents
     */
    val reportNotificationIntentStatus = ReportNotificationIntentStatus(task, allDiagnoses)

    /**
     * True if all notification are completed
     */
    val isCompleted: Boolean = reportNotificationIntentStatus.reportIntents.all { p -> p.isCompleted || !p.isNotificationDesignated || !p.isActive }

    /**
     * <pre>
     * Class for sorting the notification status by reportIntent
     *  // list of all reportIntents
     *  reportIntents (List, ReportIntentStatus bearer for ReportIntent)
     *      // list of all diagnosis revisions
     *      diagnosis (List, DiagnosisRevisionStatus bearer for DiagnosisRevision)
     *          name
     *          ...
     *          // list of a notification type + history record which is associated with the diagnosis
     *          notifications (List, NotificationTypeAndHistory bearer for  NotificationTyp + DiagnosisHistoryRecord associated with the diagnosis)
     *              history (DiagnosisHistoryRecord)
     *              notificationTyp (NotificationTyp)
     *              ...
     *
     *
     * Implements IExtendedNotificationStatusForReportIntent for displaying reasons (inner class can not be used in jsf components)
     * Display Element: OverlayNotificationStatusForReportIntent.xhtml (displays ReportIntentStatus -> DiagnosisRevisionStatus)
     * </pre>
     */
    class ReportNotificationIntentStatus(task: Task, allDiagnoses: Set<DiagnosisRevision>) {

        /**
         * List of all reportIntents
         */
        val reportIntents = task.contacts.map { ReportIntentStatus(it, allDiagnoses) }

        /**
         * This class contains a single reportIntent
         */
        class ReportIntentStatus(val reportIntent: ReportIntent, allDiagnoses: Set<DiagnosisRevision>) : IExtendedOverlayNotificationStatusForReportIntent {

            /**
             * A list of all diagnoses of associated case
             */
            override val diagnoses = allDiagnoses.map { DiagnosisRevisionStatus(it, reportIntent) }

            /**
             * Is true if every notification for this reportintent is completed
             */
            override val isCompleted: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(reportIntent)

            /**
             * True if notification is active
             */
            override val isActive = reportIntent.active

            /**
             * True if no notification is designated
             */
            override val isNotificationDesignated = SpringContextBridge.services().reportIntentService.isNotificationDesignated(reportIntent)

            /**
             * True if either not active or no notification should happen
             */
            override val isNoNotification = !isActive || !isNotificationDesignated

            /**
             * Class containing the diangosis and all notification associated with that diagnosis and the reportIntent
             */
            class DiagnosisRevisionStatus(val diagnosisRevision: DiagnosisRevision, reportIntent: ReportIntent) {

                /**
                 * Diagnosis Name
                 */
                val name: String = diagnosisRevision.name

                /**
                 * Information of the associated notifications
                 */
                val notifications = findHistoryForDiagnosis(reportIntent)

                /**
                 * Search for history data matching the given diagnosis id
                 */
                private fun findHistoryForDiagnosis(reportIntent: ReportIntent): List<NotificationTypeAndHistory> {
                    val notificationHistoryRecord = mutableListOf<NotificationTypeAndHistory>()

                    reportIntent.notifications.forEach { n ->
                        n.history.forEach { h ->
                            if (h.diagnosisID == this.diagnosisRevision.id) {
                                notificationHistoryRecord.add(NotificationTypeAndHistory(h, n.notificationTyp, n.active))
                            }
                        }
                    }

                    return notificationHistoryRecord
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
     *                  ...
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
             * Status of the notification
             */
            val notificationStatus: NotificationStatus = diagnosisRevision.notificationStatus

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

                val isNotificationDesignated = SpringContextBridge.services().reportIntentService.isNotificationDesignated(reportIntent)

                val mailRecord = notificationHistoryRecords.firstOrNull { p -> p.notificationTyp == NotificationTyp.EMAIL }
                val isMailRecord = mailRecord != null
                val isMailActive = isActive && mailRecord?.isNotificationActive ?: false
                val mailPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(mailRecord?.history
                        ?: DiagnosisHistoryRecord())

                val faxRecord = notificationHistoryRecords.firstOrNull { p -> p.notificationTyp == NotificationTyp.FAX }
                val isFaxRecord = faxRecord != null
                val isFaxActive = isActive && faxRecord?.isNotificationActive ?: false
                val faxPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(faxRecord?.history
                        ?: DiagnosisHistoryRecord())

                val letterRecord = notificationHistoryRecords.firstOrNull { p -> p.notificationTyp == NotificationTyp.LETTER }
                val isLetterRecord = letterRecord != null
                val isLetterActive = isActive && letterRecord?.isNotificationActive ?: false
                val letterPerformed: Boolean = SpringContextBridge.services().reportIntentService.isNotificationPerformed(letterRecord?.history
                        ?: DiagnosisHistoryRecord())

                val phoneRecord = notificationHistoryRecords.firstOrNull { p -> p.notificationTyp == NotificationTyp.PHONE }
                val isPhoneRecord = phoneRecord != null
                val isPhoneActive = isActive && phoneRecord?.isNotificationActive ?: false
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
                        val history = n.history.lastOrNull { h -> h.diagnosisID == this.diagnosisRevision.id }
                        if (history != null) {
                            notificationHistoryRecord.add(NotificationTypeAndHistory(history, n.notificationTyp, n.active))
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
    class NotificationTypeAndHistory(val history: DiagnosisHistoryRecord, val notificationTyp: NotificationTyp, val isNotificationActive: Boolean) {
        val success = SpringContextBridge.services().reportIntentService.isNotificationPerformed(history)
        val totalSuccesses = history.data.count { p -> !p.failed }
        val totalFailedAttempts = history.data.count { p -> p.failed }
        val totalAttempts = history.data.size
        val reportData = history.data.toList()
    }

    companion object {
        /**
         * Returns a list with all ids of current and deleted diagnosis revisions. The current ids will be taken for a
         * list of diagnosisRevisions, the deleted ids will be taken from the history of the reportIntents.
         */
        public fun findAllDiagnoses(diagnosisRevisions: Set<DiagnosisRevision>, reportIntents: Set<ReportIntent>): Set<DiagnosisRevision> {
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
}