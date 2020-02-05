package com.patho.main.model.system

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
    @Column(unique = true, nullable = false)
    @RevisionNumber
    override var id: Long = 0

    @Enumerated(EnumType.STRING)
    var listType: ListItemType? = null

    @Column(columnDefinition = "VARCHAR")
    var value: String = ""

    @Column
    override var indexInList = 0

    @Column
    var archived = false

    constructor()

    constructor(type: ListItemType) {
        this.listType = type
    }
}