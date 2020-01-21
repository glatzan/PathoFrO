package com.patho.main.util.status

import com.patho.main.model.Signature
import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.miscellaneous.Council
import com.patho.main.util.status.reportIntent.ReportIntentStatusByReportIntentAndDiagnosis


class TotalTaskStatus(task: Task) {

    val taskID = task.taskID

    val staining = StainingStatus(task)
    val diagnosis = DiagnosisStatus(task)
    val notifications = ExtendedNotificationStatus(task)
    val consultations = ConsultationStatus(task)
    val lists = FavouriteListStatus(task)

    val isArchiveAble = staining.isCompleted && diagnosis.isCompleted && notifications.isCompleted && consultations.isCompleted && lists.isNotBlockingFiling

    /**
     * Status for all slides
     */
    class StainingStatus(task: Task) {
        /**
         * Staining phase is completed
         */
        val isCompleted = task.stainingCompleted

        /**
         * Date of phase completion
         */
        val dateOfCompletion = task.stainingCompletionDate

        /**
         * All slides with their statuses
         */
        val slides = task.samples.flatMap { it -> it.blocks.flatMap { it -> it.slides.map { SlideStatus(it) } } }

        /**
         * True if all slides are marked als stained
         */
        val slidesMarkedAsCompleted = slides.all { it.isCompleted }

        /**
         * Status class for a single slide
         */
        class SlideStatus(val slide: Slide) {
            /**
             * True if completed
             */
            val isCompleted = slide.stainingCompleted

            /**
             * date of completion
             */
            val dateOfCompletion = slide.completionDate

            /**
             * slide name
             */
            val name = slide.slideID

            /**
             * true if slide is a restaining
             */
            val isReStaining = slide.reStaining
        }
    }

    /**
     * Status of all diagnoses
     */
    class DiagnosisStatus(task: Task) {
        /**
         * True if reportIntent phase is completed
         */
        val isCompleted = task.diagnosisCompleted

        /**
         * Completion date of reportIntent phase
         */
        val dateOfCompletion = task.diagnosisCompletionDate

        /**
         * Details for all reportIntent revisions
         */
        val revisions = task.diagnosisRevisions.map { RevisionStatus(it) }

        /**
         * True if all diagnoses are valid
         */
        val isValid = revisions.all { it.isValidDiagnosis }

        /**
         * Detailed status of ane reportIntent revision
         */
        class RevisionStatus(val diagnosisRevision: DiagnosisRevision) {
            /**
             * True if reportIntent is completed
             */
            val isCompleted = diagnosisRevision.completed

            /**
             *  Diagnosis name
             */
            val name = diagnosisRevision.name

            /**
             * date of completion
             */
            val dateOfCompletion = diagnosisRevision.completionDate

            /**
             * Signatures
             */
            val signatures = listOfNotNull<SignatureStatus>(getSignatureStatus(diagnosisRevision.signatureOne), getSignatureStatus(diagnosisRevision.signatureTwo))

            /**
             * True if at least one signature is present
             */
            val isSigned = signatures.isNotEmpty()

            /**
             * True if no notification should be performed
             */
            val isNoNotification = diagnosisRevision.notificationStatus == com.patho.main.model.patient.NotificationStatus.NO_NOTFICATION

            /**
             * True if notification was performed
             */
            val isNotificationPerformed = diagnosisRevision.notificationStatus == com.patho.main.model.patient.NotificationStatus.NOTIFICATION_COMPLETED || isNoNotification

            /**
             * Date of notification
             */
            val dateOfNotification = diagnosisRevision.notificationDate

            /**
             *  True if reportIntent is valid
             */
            val isValidDiagnosis = isCompleted && (isSigned || isNoNotification)

            private fun getSignatureStatus(signature: Signature): SignatureStatus? {
                val signatureStatus = SignatureStatus(signature)
                return if (signatureStatus.present) signatureStatus else null
            }

            /**
             * Signature status
             */
            class SignatureStatus(signature: Signature) {
                val present = signature.physician != null
                val name = signature?.physician?.person?.getFullName() ?: ""
            }
        }
    }

    /**
     * Notification status
     */
    public class NotificationStatus(task: Task) {
        /**
         * If notification phase is set to completed
         */
        val isCompleted = task.notificationCompleted

        /**
         * Date of notification phase completion
         */
        val dateOfCompletion = task.notificationCompletionDate

        /**
         * Singe notifications
         */
        val notification = ReportIntentStatusByReportIntentAndDiagnosis(task)

        /**
         * Returns true if all notifications are completed
         */
        val allNotificationsCompleted = notification.completed
    }


    public class ConsultationStatus(task: Task) {

        val isCompleted = task.councils.all { it.councilCompleted }

        val consultations = task.councils.map { CouncilStat(it) }

        public class CouncilStat(council: Council) {
            val name = council.name
            val isCompleted = council.councilCompleted
            val dateOfCompletion = council.councilCompletedDate
        }
    }

    /**
     * Status for Favouritelists, contains also an ordered list of favouritelists objects by groups
     */
    class FavouriteListStatus(task: Task) {

        /**
         * All lists, not sorted
         */
        val lists = task.favouriteLists

        /**
         * All lists sorted by icongroups
         */
        val listsByGroups = sortGroups(task.favouriteLists);

        /**
         *  True is no lists blocks filing
         */
        val isNotBlockingFiling = !lists.any { it.blockTaskArchival }

        /**
         * Sorts all favouriteLists by groups
         */
        private fun sortGroups(favouriteLists: MutableList<FavouriteList>): MutableList<FavouriteGroup> {
            val groups = mutableListOf<FavouriteGroup>()

            for (favouriteList in favouriteLists) {
                val currentGroup: FavouriteGroup = groups.firstOrNull { it.id == favouriteList.iconGroup } ?: run {
                    val tmp = FavouriteGroup(favouriteList.iconGroup)
                    groups.add(tmp)
                    tmp
                }

                currentGroup.addFavouriteList(favouriteList);
            }

            return groups;
        }

        /**
         * Container for an icon group
         */
        class FavouriteGroup(val id: String) {
            /**
             * List of Favourites
             */
            val favouriteLists: MutableList<FavouriteList> = mutableListOf()

            fun addFavouriteList(list: FavouriteList) {
                favouriteLists.add(list)
            }
        }
    }
}