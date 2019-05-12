package com.patho.main.util.task

import com.patho.main.model.Signature
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.miscellaneous.Council
import com.patho.main.util.status.ReportIntentStatusByReportIntentAndDiagnosis

public class ArchiveTaskStatus(task: Task) {

    val stainingStatus = StainingStatus(task)
    val diagnosisStatus = DiagnosisStatus(task)
    val notificationStatus = NotificationStatus(task)
    val councilStatus = CouncilStatus(task)
    val favouritesStatus = FavouritesStatus(task)

    val isArchiveAble = stainingStatus.isCompleted && diagnosisStatus.isCompleted && notificationStatus.isCompleted && councilStatus.isCompleted && !favouritesStatus.isArchivalBlocked

    public class StainingStatus(task: Task) {
        val isCompleted = task.stainingCompleted
        val dateOfCompletion = task.stainingCompletionDate
        val slides = task.samples.flatMap { it.blocks.flatMap { it.slides.map { SlideStatus(it) } } }

        public class SlideStatus(val slide: Slide) {
            val isCompleted = slide.stainingCompleted
            val name = slide.slideID
            val isReStaining = slide.reStaining
        }
    }

    public class DiagnosisStatus(task: Task) {
        val isCompleted = task.diagnosisCompleted
        val dateOfCompletion = task.diagnosisCompletionDate

        val revisions = task.diagnosisRevisions.map { RevisionStatus(it) }

        public class RevisionStatus(val diagnosisRevision: DiagnosisRevision) {
            val isCompleted = diagnosisRevision.completed
            val name = diagnosisRevision.name
            val signatures = listOfNotNull<SignatureStatus>(getSignatureStatus(diagnosisRevision.signatureOne), getSignatureStatus(diagnosisRevision.signatureTwo))
            val isSigned = signatures.isEmpty()

            val isNoNotification = diagnosisRevision.notificationStatus == com.patho.main.model.patient.NotificationStatus.NO_NOTFICATION
            val isNotificationPerformed = diagnosisRevision.notificationStatus == com.patho.main.model.patient.NotificationStatus.NOTIFICATION_COMPLETED || isNoNotification

            private fun getSignatureStatus(signature: Signature): SignatureStatus? {
                val signatureStatus = SignatureStatus(signature)
                return if (signatureStatus.present) signatureStatus else null
            }

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
        val blockingLists = task.favouriteLists.filter { it.blockTaskArchival }
        val isArchivalBlocked = blockingLists.isEmpty()
    }
}