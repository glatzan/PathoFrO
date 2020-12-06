package com.patho.main.model.patient

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.audit.AuditAble
import com.patho.main.model.interfaces.IdManuallyAltered
import com.patho.main.model.interfaces.Parent
import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.util.audit.Audit
import com.patho.main.model.util.audit.AuditListener
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import java.time.Instant
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@EntityListeners(AuditListener::class)
@SequenceGenerator(name = "slide_sequencegenerator", sequenceName = "slide_sequence")
open class Slide : AbstractPersistable, Parent<Block>, IdManuallyAltered, AuditAble {

    @Id
    @GeneratedValue(generator = "slide_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    @Column
    open var uniqueIDinTask: Int = 0

    @Column
    open var slideID = ""

    @Column
    open override var idManuallyAltered: Boolean = false

    /**
     * Audit Data
     */
    @Embedded
    override var audit: Audit? = null

    @Column(nullable = true)
    open var completionDate: Instant? = null

    @Column
    open var stainingCompleted: Boolean = false

    @Column
    open var reStaining: Boolean = false

    @Column(columnDefinition = "VARCHAR")
    open var commentary = ""

    @Column(columnDefinition = "VARCHAR")
    open var slideLabelText = ""

    @OneToOne
    @NotAudited
    open var slidePrototype: StainingPrototype? = null

    @ManyToOne
    open override var parent: Block? = null

    constructor() {}

    /**
     * Returns task
     */
    open override val task: Task?
        @Transient
        get() = parent?.task


    /**
     * Return parent
     */
    open override val patient: Patient?
        @Transient
        get() = parent?.patient

}
