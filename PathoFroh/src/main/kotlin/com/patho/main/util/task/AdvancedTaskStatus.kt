package com.patho.main.util.task

import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.NotificationStatus
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.util.status.ReportIntentStatus

open class AdvancedTaskStatus(task: Task) : TaskStatus(task) {

    /**
     *  True if staining phase has ended
     */
    var stainingPhaseCompleted: Boolean = false

    /**
     * True if all slides are marked as completed
     */
    val stainingCompleted: Boolean
        get() = stainingsToComplete.all { p -> p.completed }

    /**
     * The index of the opened panel
     */
    var stainingAccordionIndex: Int = 0
        get() = if (stainingCompleted) -1 else 0

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
        get() = diagnosesStatus.all { p -> (p.completed && p.signature) || p.notificationStatus.ignore }

    /**
     * The index of the opened panel
     */
    var diagnosisAccordionIndex: Int = 0
        get() = if (diagnosesCompleted) -1 else 0

    /**
     * List of diagnoses and their status
     */
    var diagnosesStatus: List<DiagnosisRevisionStatus> = ArrayList<DiagnosisRevisionStatus>()

    /**
     *  True if notification phase has ended
     */
    var notificationPhaseCompleted: Boolean = false

    /**
     * Status of the contacts
     */
    var notificationsStatus: List<ReportIntentStatus>? = null

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

        notificationPhaseCompleted = task.notificationCompleted

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
        val signature: Boolean = diagnosisRevision.signatureOne.physician != null || diagnosisRevision.signatureTwo.physician != null

        val notificationStatus: DiagnosisNotificationStatus = DiagnosisNotificationStatus(diagnosisRevision)

        public class DiagnosisNotificationStatus(diagnosisRevision: DiagnosisRevision) {
            val performed: Boolean = diagnosisRevision.notificationStatus == NotificationStatus.NOTIFICATION_COMPLETED;
            val ignore: Boolean = diagnosisRevision.notificationStatus == NotificationStatus.NO_NOTFICATION;
        }
    }
}