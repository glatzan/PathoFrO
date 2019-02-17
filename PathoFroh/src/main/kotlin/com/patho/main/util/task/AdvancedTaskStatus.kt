package com.patho.main.util.task

import com.patho.main.model.AssociatedContact
import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task

open class AdvancedTaskStatus(task: Task) : TaskStatus(task) {


    /**
     * True if all slides are marked as completed
     */
    val stainingCompleted: Boolean
        get() = stainingsToComplete.isEmpty()

    /**
     * List of slides that are not completed yet
     */
    var stainingsToComplete: List<Slide> = ArrayList<Slide>()

    /**
     * Checks if diagnoses are completed and signed or if the ignore flag is set
     */
    val diagnosesCompleted: Boolean
        get() = diagnosesStatus.any { p -> (p.completed && p.signature) || p.ignore }

    /**
     * List of diagnoses and their status
     */
    var diagnosesStatus: List<DiagnosisRevisionStatus> = ArrayList<DiagnosisRevisionStatus>()

    /**
     * Checks if a notification was performed for all diagnoses
     */
    val notificationCompleted: Boolean
        get() = notificationsOfDiagnoses.any { p -> p.completed || p.ignore }

    /**
     * Status of the single diagnoses
     */
    var notificationsOfDiagnoses: List<DiagnosisRevisionStatus> = ArrayList<DiagnosisRevisionStatus>()

    /**
     * Status of the contacts
     */
    var notificationsStatus: List<NotificationStatus> = ArrayList<NotificationStatus>()

    /**
     * Contains all favourite list of the task
     */
    var favouriteListOfTask: List<FavouriteList> = ArrayList<FavouriteList>()

    /**
     * Contains all favourite lists of a task which are blocking the archive process
     */
    var blockingFavouriteListsOfTask: List<FavouriteList> = ArrayList<FavouriteList>()


    override fun generateStatus(): TaskStatus {
        logger.debug("Generating simple Taskstatus for {}", task.taskID)
        super.generateStatus()

        val stainingsToComplete = ArrayList<Slide>();

        task.samples.forEach {
            it.blocks.forEach {
                it.slides.forEach {
                    if (!it.stainingCompleted) stainingsToComplete.add(it)
                }
            }
        }

        this.stainingsToComplete = stainingsToComplete

        diagnosesStatus = task.diagnosisRevisions.map { p -> DiagnosisRevisionStatus(p) }

        return this
    }

    /**
     * Status for diagnoses
     */
    public class DiagnosisRevisionStatus(diagnosisRevision: DiagnosisRevision) {
        val completed: Boolean = diagnosisRevision.completed
        val signature: Boolean = diagnosisRevision.signatureOne != null || diagnosisRevision.signatureTwo != null
        val ignore: Boolean = false

        val notificationStatus: DiagnosisNotificationStatus = DiagnosisNotificationStatus(diagnosisRevision)
    }

    public class DiagnosisNotificationStatus(diagnosisRevision: DiagnosisRevision) {
        val perfomed: Boolean = diagnosisRevision.notificationStatus == DiagnosisRevision.NotificationStatus.NOTIFICATION_COMPLETED;
        val igonre: Boolean = diagnosisRevision.notificationStatus == DiagnosisRevision.NotificationStatus.NO_NOTFICATION;
    }

    public class NotificationStatus(associatedContact: AssociatedContact) {
        var completed: Boolean = associatedContact.isNotificationPerformed
    }
}