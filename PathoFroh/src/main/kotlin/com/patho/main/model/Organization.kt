package com.patho.main.model

import com.patho.main.model.interfaces.ID
import javax.persistence.*

open class Organization : AbstractPersistable, ID {

    @Id
    @GeneratedValue(generator = "organization_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    @Column(columnDefinition = "VARCHAR", unique = true)
    open var name: String = ""

    @OneToOne(cascade = [CascadeType.ALL])
    open var contact: Contact? = null

    @Column(columnDefinition = "VARCHAR")
    open var note: String = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "organizsations")
    open var persons: List<Person>? = null

    @Column
    open var intern: Boolean = false

    @Column(columnDefinition = "boolean default true")
    open var archived = false

    constructor()

    constructor(contact: Contact) {
        this("", contact, false)
    }

    constructor(name: String, contact: Contact, intern: Boolean) {
        this.name = name
        this.contact = contact
        this.intern = intern
    }
}
