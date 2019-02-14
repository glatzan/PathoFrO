package com.patho.main.model.patient

import com.patho.main.common.DiagnosisRevisionType
import com.patho.main.model.Signature
import com.patho.main.model.interfaces.ID
import com.patho.main.model.interfaces.Parent
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "diagnosisRevision_sequencegenerator", sequenceName = "diagnosisRevision_sequence")
open class DiagnosisRevision : ID, Parent<Task> {

    /**
     * Internal marker for a diagnosis
     */
    companion object {
        const val MARKER_COUNCIL = "c:\$id"
    }

    @Id
    @GeneratedValue(generator = "diagnosisRevision_sequencegenerator")
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
     * Date of diagnosis creation.
     */
    @Column
    open var creationDate: Long = 0

    /**
     * Date of diagnosis finalization.
     */
    @Column
    open var completionDate: Long = 0

    /**
     * Status of the notification, e.g. pending, completed
     */
    @Enumerated(EnumType.ORDINAL)
    open var notificationStatus = NotificationStatus.NOT_APPROVED

    /**
     * Date of notification
     */
    @Column
    open var notificationDate: Long = 0

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
    open var signatureOne: Signature? = null

    /**
     * Selected consultant to sign the report
     */
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    open var signatureTwo: Signature? = null

    /**
     * Date of the signature
     */
    @Column
    open var signatureDate: LocalDate = LocalDate.now()

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

    open override fun toString(): String {
        return "Diagnosis-Revision: $name, ID: $id"
    }

    open override fun equals(obj: Any?): Boolean {
        return if (obj is DiagnosisRevision && obj.id == id) true else super.equals(obj)
    }

    enum class NotificationStatus {
        /**
         * Not approved yet
         */
        NOT_APPROVED,
        /**
         * Notification is pending
         */
        NOTIFICATION_PENDING,
        /**
         * No Notification should be performed
         */
        NO_NOTFICATION,
        /**
         * Notification was performed
         */
        NOTIFICATION_COMPLETED
    }
}