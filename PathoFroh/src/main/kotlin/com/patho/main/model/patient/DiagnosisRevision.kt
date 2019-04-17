package com.patho.main.model.patient

import com.patho.main.common.DiagnosisRevisionType
import com.patho.main.model.AbstractPersistable
import com.patho.main.model.Signature
import com.patho.main.model.audit.AuditAble
import com.patho.main.model.interfaces.Parent
import com.patho.main.model.util.audit.Audit
import com.patho.main.model.util.audit.AuditListener
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import java.time.Instant
import java.time.LocalDate
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
@EntityListeners(AuditListener::class)

open class DiagnosisRevision : AbstractPersistable, AuditAble, Parent<Task> {

    /**
     * Internal marker for a diagnosis
     */
    companion object {
        const val MARKER_COUNCIL = "c:\$id"
    }

    @Id
    @GeneratedValue(generator = "diagnosisRevision_sequencegenerator")
    @SequenceGenerator(name = "diagnosisRevision_sequencegenerator", sequenceName = "diagnosisRevision_sequence")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    /**
     * Name of this revision
     */
    @Column
    open var name: String = ""

    /**
     * Version
     */
    @Version
    open var version: Long = 0

    /**
     * Parent of the Diagnosis
     */
    @ManyToOne(targetEntity = Task::class)
    open override var parent: Task? = null

    /**
     * Type of the revison @see [DiagnosisRevisionType]
     */
    @Enumerated(EnumType.STRING)
    open var type: DiagnosisRevisionType? = null

    /**
     * Audit Data
     */
    @Embedded
    open override var audit: Audit? = null

    /**
     * Date of diagnosis finalization.
     */
    @Column
    open var completionDate: Instant? = null

    /**
     * Returns true if completion date is set
     */
    open val completed: Boolean
        @Transient
        get() = completionDate != null

    /**
     * Status of the notification, e.g. pending, completed
     */
    @Enumerated(EnumType.ORDINAL)
    open var notificationStatus = NotificationStatus.NOT_APPROVED

    /**
     * Date of notification
     */
    @Column
    open var notificationDate: Instant? = null

    /**
     * Returns true if notification date is set
     */
    open val isNotified: Boolean
        @Transient
        get() = notificationDate != null


    /**
     * Date of the signature
     */
    @Column
    open var signatureDate: LocalDate = LocalDate.now()

    /**
     * Internal references, e.g. to a council
     */
    @Column(length = 10)
    open var intern: String? = null

    /**
     * All diagnoses
     */
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "parent", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("sample.id ASC")
    open var diagnoses = mutableListOf<Diagnosis>()

    /**
     * Text containing the histological record for all samples.
     */
    @Column(columnDefinition = "text")
    open var text = ""

    /**
     * Selected physician to sign the report
     */
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    open var signatureOne: Signature = Signature()

    /**
     * Selected consultant to sign the report
     */
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    open var signatureTwo: Signature = Signature()

    constructor() {
    }

    constructor(name: String, type: DiagnosisRevisionType) {
        this.name = name
        this.type = type
    }

    constructor(parent: Task, type: DiagnosisRevisionType) {
        this.parent = parent
        this.type = type
    }

    open val isMalign: Boolean
        @Transient
        get() = diagnoses.any { p -> p.malign }

    open override val patient: Patient?
        @Transient
        get() = parent?.patient

    open override val task: Task?
        @Transient
        get() = parent?.task
}