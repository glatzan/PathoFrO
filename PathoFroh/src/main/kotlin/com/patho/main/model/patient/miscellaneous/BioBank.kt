package com.patho.main.model.patient.miscellaneous

import com.patho.main.common.InformedConsentType
import com.patho.main.model.AbstractPersistable
import com.patho.main.model.PDFContainer
import com.patho.main.model.audit.AuditAble
import com.patho.main.model.interfaces.DataList
import com.patho.main.model.interfaces.ID
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.util.audit.Audit
import com.patho.main.model.util.audit.AuditListener
import org.hibernate.annotations.*
import org.hibernate.envers.Audited
import java.util.*
import javax.persistence.*
import javax.persistence.Entity
import javax.persistence.OrderBy

@Entity
@Audited
@SelectBeforeUpdate
@SequenceGenerator(name = "bioBank_sequencegenerator", sequenceName = "bioBank_sequence")
@EntityListeners(AuditListener::class)
open class BioBank : AbstractPersistable, DataList, AuditAble {

    @Id
    @GeneratedValue(generator = "bioBank_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    @OneToOne(fetch = FetchType.LAZY)
    open var task: Task? = null

    @Enumerated(EnumType.STRING)
    open var informedConsentType: InformedConsentType? = null

    @Embedded
    open override var audit: Audit? = null

    /**
     * Date of informed constent retraction
     */
    @Temporal(TemporalType.DATE)
    open var retractionDate: Date? = null

    /**
     * Date of informed constent retraction
     */
    @Temporal(TemporalType.DATE)
    open var consentDate: Date? = null

    /**
     * Text of council
     */
    @Column(columnDefinition = "text")
    open var commentary: String = ""

    @OneToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("audit.createdOn DESC")
    open override var attachedPdfs: MutableSet<PDFContainer> = HashSet<PDFContainer>()

    constructor() {}

    override val publicName: String
        @Transient
        get() = "BioBank - " + task?.taskID

    override val patient: Patient?
        @Transient
        get() = task?.patient

}