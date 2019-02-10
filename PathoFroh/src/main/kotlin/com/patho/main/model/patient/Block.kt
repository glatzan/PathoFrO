package com.patho.main.model.patient

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ID
import com.patho.main.model.interfaces.IdManuallyAltered
import com.patho.main.model.interfaces.Parent
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "block_sequencegenerator", sequenceName = "block_sequence")
open class Block : AbstractPersistable(), ID, Parent<Sample>, IdManuallyAltered {

    @Id
    @GeneratedValue(generator = "block_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    /**
     * Parent of this block
     */
    @ManyToOne
    open override var parent: Sample? = null

    /**
     * ID in block
     */
    @Column
    open var blockID: String = ""

    /**
     * True if the user has manually altered the sample ID
     */
    @Column
    open override var idManuallyAltered: Boolean = false

    /**
     * staining array
     */
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, mappedBy = "parent")
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("creationDate ASC, id ASC")
    open var slides= mutableListOf<Slide>()

    /**
     * Date of sample creation
     */
    @Column
    open var creationDate: Long = 0

    open override val patient: Patient?
        @Transient
        get() = parent?.patient

    open override val task: Task?
        @Transient
        get() = parent?.task
}