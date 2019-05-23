package com.patho.main.util.task

import com.patho.main.model.Signature
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.miscellaneous.Council
import com.patho.main.util.status.diagnosis.ReportIntentStatusByReportIntentAndDiagnosis

public class ArchiveTaskStatus(task: Task) {

    val stainingStatus = StainingStatus(task)
    val diagnosisStatus = DiagnosisStatus(task)
    val notificationStatus = NotificationStatus(task)
    val councilStatus = CouncilStatus(task)
    val favouritesStatus = FavouritesStatus(task)

    val isArchiveAble = stainingStatus.isCompleted && diagnosisStatus.isCompleted && notificationStatus.isCompleted && councilStatus.isCompleted && !favouritesStatus.isCompleted

    /**
     * Status for all slides
     */
    public class StainingStatus(task: Task) {
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
        val slides = task.samples.flatMap { it.blocks.flatMap { it.slides.map { SlideStatus(it) } } }

        /**
         * True if all slides are marked als stained
         */
        val allSlidesCompleted = slides.all { it.isCompleted }

        /**
         * Status class for a single slide
         */
        public class SlideStatus(val slide: Slide) {
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
    public class DiagnosisStatus(task: Task) {
        /**
         * True if diagnosis phase is completed
         */
        val isCompleted = task.diagnosisCompleted

        /**
         * Completion date of diagnosis phase
         */
        val dateOfCompletion = task.diagnosisCompletionDate

        /**
         * Details for all diagnosis revisions
         */
        val revisions = task.diagnosisRevisions.map { RevisionStatus(it) }

        /**
         * True if all diagnoses are valid
         */
        val isValid = revisions.all { it.isValid }

        /**
         * Detailed status of ane diagnosis revision
         */
        public class RevisionStatus(val diagnosisRevision: DiagnosisRevision) {
            /**
             * True if diagnosis is completed
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
             *  True if diagnosis is valid
             */
            val isValid = isCompleted && (isSigned || isNoNotification)

            private fun getSignatureStatus(signature: Signature): SignatureStatus? {
                val signatureStatus = SignatureStatus(signature)
                return if (signatureStatus.present) signatureStatus else null
            }

            /**
             * Signature status
             */
            public class SignatureStatus(signature: Signature) {
                val present = signature.physician != null
                val name = signature?.physician?.person?.getFullName() ?: ""
            }
        }
    }

    public class NotificationStatus(task: Task) {
        val isCompleted = task.notificationCompleted
        val dateOfCompletion = task.notificationCompletionDate
        val notification = ReportIntentStatusByReportIntentAndDiagnosis(task)
    }

    public class CouncilStatus(task: Task) {
        val isCompleted = task.councils.all { it.councilCompleted }
        val councils = task.councils.map { CouncilStat(it) }

        public class CouncilStat(council: Council) {
            val name = council.name
            val isCompleted = council.councilCompleted
            val dateOfCompletion = council.councilCompletedDate
        }
    }

    public class FavouritesStatus(task: Task) {
        val lists = task.favouriteLists
        val isCompleted = !lists.any { it.blockTaskArchival }
    }
}