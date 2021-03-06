package com.patho.main.model.patient

import com.patho.main.common.ContactRole
import com.patho.main.common.Eye
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.common.TaskPriority
import com.patho.main.model.AbstractPersistable
import com.patho.main.model.PDFContainer
import com.patho.main.model.audit.AuditAble
import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.interfaces.DataList
import com.patho.main.model.interfaces.Parent
import com.patho.main.model.patient.miscellaneous.Council
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.person.Person
import com.patho.main.model.util.audit.Audit
import com.patho.main.model.util.audit.AuditListener
import com.patho.main.util.task.TaskStatus
import org.hibernate.annotations.*
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import java.time.Instant
import java.time.LocalDate
import javax.persistence.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OrderBy

@Entity
@Audited
@SelectBeforeUpdate(true)
@EntityListeners(AuditListener::class)
open class Task : AbstractPersistable, Parent<Patient>, AuditAble, DataList {

    @Id
    @GeneratedValue(generator = "task_sequencegenerator")
    @SequenceGenerator(name = "task_sequencegenerator", sequenceName = "task_sequence")
    @Column(unique = true, nullable = false)
    override open var id: Long = 0

    @Version
    open var version: Long = 0

    /**
     * Generated Task ID as String
     */
    @Column(length = 6)
    open var taskID = ""

    /**
     * The Patient of the task;
     */
    @ManyToOne
    open override var parent: Patient? = null

    /**
     * If true the program will provide default names for samples and blocks
     */
    @Column
    open var useAutoNomenclature: Boolean = false

    /**
     * Audit Data
     */
    @Embedded
    override var audit: Audit? = null

    /**
     * The date of the sugery
     */
    @Column
    open var dateOfSugery: LocalDate = LocalDate.now()

    /**
     * Date of reception of the first material
     */
    @Column
    open var receiptDate: LocalDate = LocalDate.now()

    /**
     * The dueDate
     */
    @Column
    open var dueDate: LocalDate = LocalDate.now()

    /**
     * Priority of the task
     */
    @Enumerated(EnumType.ORDINAL)
    open var taskPriority: TaskPriority = TaskPriority.NONE

    /**
     */
    @Column
    open var typeOfOperation: Byte = 0

    /**
     * Details of the case
     */
    @Column(columnDefinition = "VARCHAR")
    open var caseHistory: String = ""

    /**
     * Details of the case
     */
    @Column(columnDefinition = "VARCHAR")
    open var commentary: String = ""

    /**
     * Insurance of the patient
     */
    @Column(columnDefinition = "VARCHAR")
    open var insurance: String = ""

    /**
     * Ward of the patient
     */
    @Column
    open var ward: String = ""

    /**
     * Ey of the samples right/left/both
     */
    @Enumerated(EnumType.STRING)
    open var eye = Eye.UNKNOWN

    /**
     * date of staining completion
     */
    @Column
    open var stainingCompletionDate: Instant? = null

    /**
     * True if stating completion date is not null
     */
    open val stainingCompleted: Boolean
        @Transient
        get() = stainingCompletionDate != null

    /**
     * Date of reportIntent finalization
     */
    @Column
    open var diagnosisCompletionDate: Instant? = null

    /**
     * Returns true if reportIntent completion date is set
     */
    open val diagnosisCompleted: Boolean
        @Transient
        get() = diagnosisCompletionDate != null

    /**
     * The date of the completion of the notification.
     */
    @Column
    open var notificationCompletionDate: Instant? = null

    /**
     * Returns true if notification completion date is set
     */
    open val notificationCompleted: Boolean
        @Transient
        get() = notificationCompletionDate != null

    /**
     * The date of the finalization.
     */
    @Column
    open var finalizationDate: Instant? = null

    /**
     * True if the task can'special.pdfOrganizerDialog is completed
     */
    @Column
    open var finalized: Boolean = false

    /**
     * Unique slide counter is increased for every added slide;
     */
    open var slideCounter = 0

    /**
     * List of all persons associated with this task
     */
    // TODO fetch lazy
    @OneToMany(cascade = [CascadeType.REMOVE, CascadeType.DETACH], fetch = FetchType.EAGER, mappedBy = "task")
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("id ASC")
    @NotAudited
    open var contacts = mutableSetOf<ReportIntent>()

    /**
     * List with all samples
     */
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("id ASC")
    open var samples = mutableListOf<Sample>()

    /**
     * All diangnoses
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @OrderBy("id ASC")
    open var diagnosisRevisions = mutableSetOf<DiagnosisRevision>()

    /**
     * Generated PDFs of this task, lazy
     */
    @OneToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("audit.createdOn DESC")
    open override var attachedPdfs = mutableSetOf<PDFContainer>()

    /**
     * List of all councils of this task, lazy
     */
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "task")
    @OrderBy("dateOfRequest DESC")
    open var councils = mutableSetOf<Council>()

    /**
     * List of all favorite Lists in which the task is listed
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("id ASC")
    @NotAudited
    open var favouriteLists = mutableListOf<FavouriteList>()

    /**
     * If set to true, this task is shown in the navigation column on the left hand
     * side, however there are actions to perform or not.
     */
    @Transient
    open var active: Boolean = false

    /**
     * Contains static list entries for the gui, improves reload speed
     */
    @Transient
    open lateinit var taskStatus: TaskStatus

    constructor() {}

    constructor(id: Long) {
        this.id = id
        taskStatus = TaskStatus(this)
    }

    constructor(parent: Patient) {
        this.parent = parent
        taskStatus = TaskStatus(this)
    }

    /**
     * Sets the due date as selected
     */
    @Transient
    open fun setDueDateSelected(dueDate: Boolean) {
        taskPriority = if (dueDate) TaskPriority.TIME else TaskPriority.NONE
    }

    /**
     * Returns true if priority is set to TaskPriority.Time
     */
    @Transient
    open fun getDueDateSelected(): Boolean {
        return taskPriority == TaskPriority.TIME
    }


    /**
     * Creates linear list of all slides of the given task. The StainingTableChosser
     * is used as holder class in order to offer an option to select the slides by
     * clicking on a backend. Archived elements will not be shown if showArchived
     * is false.
     */

    @Transient
    fun generateTaskStatus(): TaskStatus {
        logger.debug("Generating taskstatus for " + taskID + " " + hashCode())
        if (!::taskStatus.isInitialized) {
            taskStatus = TaskStatus(this)
        }

        return taskStatus.generateStatus()
    }

    @Transient
    fun isActiveOrActionPending(): Boolean {
        return isActiveOrActionPending(false)
    }

    /**
     * Returns true if the task is marked as active or an action is pending. If
     * activeOnly is true only the active attribute of the task will be evaluated.
     *
     * @return
     */
    @Transient
    fun isActiveOrActionPending(activeOnly: Boolean): Boolean {
        if (activeOnly)
            return active

        if (active)
            return true

        return if (TaskStatus.hasFavouriteLists(this, PredefinedFavouriteList.StainingList, PredefinedFavouriteList.ReStainingList,
                        PredefinedFavouriteList.StayInStainingList, PredefinedFavouriteList.DiagnosisList,
                        PredefinedFavouriteList.ReDiagnosisList, PredefinedFavouriteList.StayInDiagnosisList,
                        PredefinedFavouriteList.NotificationList, PredefinedFavouriteList.StayInNotificationList)) true else false

    }

    @Transient
    fun containsContact(person: Person): Boolean {
        return contacts.any { p -> p.person == person }
    }

    @Transient
    fun containsContact(reportIntent: ReportIntent): Boolean {
        return contacts.any { p -> p == reportIntent }
    }

    @Transient
    fun getNextSlideNumber(): Int {
        return ++slideCounter
    }

    override val publicName: String
        @Transient
        get() = taskID

    /**
     * Returns task
     */
    open override val task: Task?
        @Transient
        get() = this


    /**
     * Return parent
     */
    open override val patient: Patient?
        @Transient
        get() = parent?.patient
}