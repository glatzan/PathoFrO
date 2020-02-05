package com.patho.main.model.preset

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.StainingPrototype
import com.patho.main.model.interfaces.ListOrder
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.SelectBeforeUpdate
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class MaterialPreset : ListOrder<MaterialPreset>, AbstractPersistable {
    @Id
    @GeneratedValue(generator = "materialPreset_sequencegenerator")
    @SequenceGenerator(name = "materialPreset_sequencegenerator", sequenceName = "materialPreset_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @Column
    open var name: String = ""

    @Column(columnDefinition = "text")
    open var commentary: String = ""

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    open var stainingPrototypes: MutableList<StainingPrototype> = mutableListOf()

    /**
     * On every selection of the material, this number will be increased. The
     * materials can be ordered according to this value. So often used materials
     * will be displayed first.
     */
    @Column
    open var priorityCount = 0

    @Column
    override var indexInList = 0

    @Column
    open var archived = false

    constructor()

    constructor(preset : MaterialPreset){
        this.id = preset.id
        this.name = preset.name
        this.commentary = preset.commentary
        this.stainingPrototypes = preset.stainingPrototypes.toMutableList()
    }
}