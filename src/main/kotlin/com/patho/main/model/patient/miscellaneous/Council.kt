package com.patho.main.model.patient.miscellaneous

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.PDFContainer
import com.patho.main.model.Physician
import com.patho.main.model.audit.AuditAble
import com.patho.main.model.interfaces.DataList
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.util.audit.Audit
import com.patho.main.model.util.audit.AuditListener
import com.patho.main.util.helper.TextToLatexConverter
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import kotlin.collections.HashSet

@Entity
@Audited
@SelectBeforeUpdate(true)
@SequenceGenerator(name = "council_sequencegenerator", sequenceName = "council_sequence")
@EntityListeners(AuditListener::class)
open class Council : AbstractPersistable, DataList, AuditAble {

    @Id
    @GeneratedValue(generator = "council_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    @Embedded
    open override var audit: Audit? = null

    @ManyToOne(fetch = FetchType.LAZY)
    open var task: Task? = null

    /**
     * Name of the council
     */
    @Column(columnDefinition = "VARCHAR")
    open var name: String = ""

    /**
     * Council physician
     */
    @OneToOne
    open var councilPhysician: Physician? = null

    /**
     * Physician to sign the council
     */
    @OneToOne
    open var physicianRequestingCouncil: Physician? = null

    /**
     * Date of request
     */
    open var dateOfRequest: LocalDate? = null

    /**
     * Text of council
     */
    @Column(columnDefinition = "text")
    open var councilText: String? = null

    /**
     * True if samples were send to external clinics
     */
    @Column
    open var councilRequestCompleted: Boolean = false

    /**
     * Date of request completed
     */
    open var councilRequestCompletedDate: LocalDate? = null

    /**
     * State of the council
     */
    @Enumerated(EnumType.STRING)
    open var notificationMethod: CouncilNotificationMethod? = null

    /**
     * True if samples were send to external clinics
     */
    @Column
    open var sampleShipped: Boolean = false

    /**
     * Date of request
     */
    open var sampleShippedDate: LocalDate? = dateOfRequest

    /**
     * Attached slides of the council
     */
    @Column(columnDefinition = "text")
    open var sampleShippedCommentary: String? = null

    /**
     * True if the samples should be returned
     */
    @Column
    open var expectSampleReturn: Boolean = false

    /**
     * True if sample is returned
     */
    @Column
    open var sampleReturned: Boolean = false

    /**
     * Date of request
     */
    open var sampleReturnedDate: LocalDate? = null

    /**
     * Commentary
     */
    @Column(columnDefinition = "text")
    open var sampleReturnedCommentary: String? = null

    /**
     * True if answer was received
     */
    @Column
    open var replyReceived: Boolean = false

    /**
     * Date of return
     */
    open var replyReceivedDate: LocalDate? = null

    /**
     * True if sample is returned
     */
    @Column
    open var councilCompleted: Boolean = false

    /**
     * Date of request
     */
    open var councilCompletedDate: LocalDate? = null

    /**
     * Commentary
     */
    @Column(columnDefinition = "text")
    open var commentary: String? = null

    /**
     * Pdf attached to this council
     */
    @OneToMany(fetch = FetchType.LAZY)
    @OrderBy("audit.createdOn DESC")
    open override var attachedPdfs: MutableSet<PDFContainer> = HashSet<PDFContainer>()

    constructor()

    constructor(task: Task) {
        this.task = task
    }

    @Transient
    open fun getCouncilTextAsLatex(): String {
        return TextToLatexConverter().convertToTex(councilText)
    }

    @Transient
    open fun isShippentExpected(): Boolean {
        return notificationMethod != CouncilNotificationMethod.NONE
    }

    override val publicName: String
        @Transient
        get() = "Konsil - " + task?.taskID

    override val patient: Patient?
        @Transient
        get() = task?.patient

    /**
     * Method of notification
     *
     * @author andi
     */
    enum class CouncilNotificationMethod {
        MTA, SECRETARY, NONE
    }
}
