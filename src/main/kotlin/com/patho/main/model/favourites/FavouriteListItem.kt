package com.patho.main.model.favourites

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.SelectBeforeUpdate
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class FavouriteListItem : AbstractPersistable {

    @Id
    @SequenceGenerator(name = "favouritelistitem_sequencegenerator", sequenceName = "favouritelistitem_sequence")
    @GeneratedValue(generator = "favouritelistitem_sequencegenerator")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    open lateinit var favouriteList: FavouriteList

    @OneToOne
    open lateinit var task: Task

    @Column
    open var commentary: String = ""

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    open var slides: MutableList<Slide> = mutableListOf()

    constructor()

    constructor(favouriteList: FavouriteList, task: Task) {
        this.task = task
        this.favouriteList = favouriteList
    }
}