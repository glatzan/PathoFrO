package com.patho.main.util.task

import com.patho.main.model.AssociatedContact
import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task

open class AdvancedTaskStatus(task: Task) : TaskStatus(task) {

    /**
     *  True if staining phase has ended
     */
    var stainingPhaseCompleted: Boolean = false

    /**
     * True if all slides are marked as completed
     */
    val stainingCompleted: Boolean
        get() = stainingsToComplete.any { p -> p.completed }

    /**
     * The index of the opened panel
     */
    var stainingAccordionIndex: Int = 0
        get() = if (stainingCompleted) 1 else 0

    /**
     * List of slides that are not completed yet
     */
    var stainingsToComplete: List<SlideStatus> = ArrayList<SlideStatus>()

    /**
     * True if diagnosis phase has ended
     */
    var diagnosisPhaseCompleted: Boolean = false

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


    override fun generateStatus(): AdvancedTaskStatus {
        logger.debug("Generating simple Taskstatus for {}", task.taskID)
        super.generateStatus()

        stainingPhaseCompleted = task.stainingCompleted

        val stainingsToComplete = ArrayList<SlideStatus>();

        task.samples.forEach {
            it.blocks.forEach {
                it.slides.forEach {
                    stainingsToComplete.add(SlideStatus(it))
                }
            }
        }

        this.stainingsToComplete = stainingsToComplete

        diagnosisPhaseCompleted = task.diagnosisCompleted

        diagnosesStatus = task.diagnosisRevisions.map { p -> DiagnosisRevisionStatus(p) }

        return this
    }

    /**
     * Class for displaying staining status
     */
    public class SlideStatus(slide: Slide) {
        val completed: Boolean = slide.stainingCompleted
        val name: String = slide.slideID
    }

    /**
     * Status for diagnoses
     */
    public class DiagnosisRevisionStatus(diagnosisRevision: DiagnosisRevision) {
        val name = diagnosisRevision.name
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