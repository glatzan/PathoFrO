package com.patho.main.model.patient

import com.patho.main.common.ContactRole
import com.patho.main.common.Eye
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.common.TaskPriority
import com.patho.main.model.AbstractPersistable
import com.patho.main.model.AssociatedContact
import com.patho.main.model.PDFContainer
import com.patho.main.model.Person
import com.patho.main.model.audit.AuditAble
import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.interfaces.DataList
import com.patho.main.model.interfaces.ID
import com.patho.main.model.interfaces.Parent
import com.patho.main.model.patient.miscellaneous.Council
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
open class Task : AbstractPersistable, ID, Parent<Patient>, AuditAble, DataList {

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
    open override var audit: Audit? = null

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
     * Date of diagnosis finalization
     */
    @Column
    open var diagnosisCompletionDate: Instant? = null

    /**
     * The date of the completion of the notificaiton.
     */
    @Column
    open var notificationCompletionDate: Instant? = null

    /**
     * The date of the finalization.
     */
    @Column
    open var finalizationDate: Instant? = null

    /**
     * True if the task can't is completed
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
    open var contacts = mutableSetOf<AssociatedContact>()

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
    open var taskStatus: TaskStatus = TaskStatus(this)

    /**
     * Returns true if priority is set to TaskPriority.Time
     */
    open val dueDateSelected: Boolean
        @Transient
        get() = taskPriority == TaskPriority.TIME

    constructor() {}

    constructor(id: Long) {
        this.id = id
    }

    constructor(parent: Patient) {
        this.parent = parent
    }

    @Transient
    fun getPrimarySurgeon(): AssociatedContact? {
        return getPrimaryContact(ContactRole.SURGEON, ContactRole.EXTERNAL_SURGEON)
    }

    @Transient
    fun getPrimaryPrivatePhysician(): AssociatedContact? {
        return getPrimaryContact(ContactRole.PRIVATE_PHYSICIAN)
    }

    @Transient
    fun getPrimaryContactAsString(vararg contactRole: String): AssociatedContact? {
        return getPrimaryContact(*contactRole.map { p -> ContactRole.valueOf(p) }.toTypedArray())
    }

    /**
     * Returns a associatedContact marked als primary with the given role.
     *
     * @param contactRole
     * @return
     */
    @Transient
    fun getPrimaryContact(vararg contactRole: ContactRole): AssociatedContact? {
        for (associatedContact in contacts) {
            for (i in contactRole.indices) {
                if (associatedContact.role == contactRole[i])
                    return associatedContact
            }
        }
        return null
    }

    /**
     * Creates linear list of all slides of the given task. The StainingTableChosser
     * is used as holder class in order to offer an option to select the slides by
     * clicking on a checkbox. Archived elements will not be shown if showArchived
     * is false.
     */

    @Transient
    fun generateTaskStatus(): TaskStatus {
        logger.debug("Generating taskstatus for " + taskID + " " + hashCode())
        return taskStatus.simpleStatus()
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
        return if (contacts != null) contacts.stream().anyMatch({ p -> p.getPerson() == person }) else false
    }

    @Transient
    fun containsContact(associatedContact: AssociatedContact): Boolean {
        return if (contacts != null) contacts.stream().anyMatch({ p -> p == associatedContact }) else false
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