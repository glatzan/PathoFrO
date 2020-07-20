package com.patho.main.model.preset

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ListOrder
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.RevisionNumber
import javax.persistence.*


@Entity
@SelectBeforeUpdate(true)
open class ListItem : ListOrder<ListItem>, AbstractPersistable {

    @Id
    @GeneratedValue(generator = "listItem_sequencegenerator")
    @SequenceGenerator(name = "listItem_sequencegenerator", sequenceName = "listItem_sequence")
    @Column(unique = true, nullable = false)
    @RevisionNumber
    override var id: Long = 0

    @Enumerated(EnumType.STRING)
    open var listType: ListItemType? = null

    @Column(columnDefinition = "VARCHAR")
    open var value: String = ""

    @Column
    override var indexInList = 0

    @Column
    open var archived = false

    constructor()

    constructor(type: ListItemType) {
        this.listType = type
    }
}