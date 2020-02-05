package com.patho.main.model.preset

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.StainingPrototypeDetails
import com.patho.main.model.interfaces.ListOrder
import org.hibernate.annotations.SelectBeforeUpdate
import java.util.*
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class StainingPrototype : ListOrder<MaterialPreset>, AbstractPersistable {

    companion object{

        const val TYPE_NORMAL = 0

        const val TYPE_IMMUN = 1
    }

    @Id
    @GeneratedValue(generator = "stainingPrototype_sequencegenerator")
    @SequenceGenerator(name = "stainingPrototype_sequencegenerator", sequenceName = "stainingPrototype_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @Column(columnDefinition = "VARCHAR")
    open var name: String? = null

    @Column(columnDefinition = "VARCHAR")
    open var commentary: String? = null

    @Enumerated(EnumType.STRING)
    open var type: StainingType? = null

    @Column
    open var archived = false

    /**
     * On every selection of the staining, this number will be increased. The
     * staining can be ordered according to this value. So often used stainings
     * will be displayed first.
     */
    @Column
    open var priorityCount = 0

    /**
     * for sorting
     */
    @Column
    override var indexInList = 0

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "staining")
    @OrderColumn(name = "INDEX")
    private val batchDetails: List<StainingPrototypeDetails> = ArrayList()


    constructor()
}