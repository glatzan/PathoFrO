package com.patho.main.model.patient

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.MaterialPreset
import com.patho.main.model.interfaces.ID
import com.patho.main.model.interfaces.IdManuallyAltered
import com.patho.main.model.interfaces.Parent
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import java.util.*
import javax.persistence.*

/**
 * Sample
 */
@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "sample_sequencegenerator", sequenceName = "sample_sequence")
open class Sample : AbstractPersistable(), ID, Parent<Task>, IdManuallyAltered {

    @Id
    @GeneratedValue(generator = "sample_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    /**
     * Parent of this sample.
     */
    @ManyToOne(targetEntity = Task::class)
    open override var parent: Task? = null

    /**
     * Sample ID as string
     */
    @Column
    open var sampleID: String = ""

    /**
     * True if the user has manually altered the sample ID
     */
    @Column
    open override var idManuallyAltered: Boolean = false

    /**
     * Date of sample creation
     */
    @Column
    open var creationDate: Long = 0

    /**
     * blocks array
     */
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "parent", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("blockID ASC")
    open var blocks = mutableListOf<Block>()

    /**
     * Material name is first initialized with the name of the typeOfMaterial. Can
     * be later changed.
     */
    open var material = ""

    /**
     * Material object, containing preset for staining
     */
    @OneToOne
    @NotAudited
    open var materialPreset: MaterialPreset? = null

    /**
     * Returns task
     */
    open override val task: Task?
        @Transient
        get() = parent


    /**
     * Return parent
     */
    open override val patient: Patient?
        @Transient
        get() = task?.patient
}
