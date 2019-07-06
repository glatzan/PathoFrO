package com.patho.main.model.person

import com.patho.main.model.AbstractPersistable
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@Audited
open class Organization : AbstractPersistable {

    @Id
    @GeneratedValue(generator = "organization_sequencegenerator")
    @SequenceGenerator(name = "organization_sequencegenerator", sequenceName = "organization_sequence" , allocationSize = 1)
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    @Column(columnDefinition = "VARCHAR", unique = true)
    open var name: String = ""

    @OneToOne(cascade = [CascadeType.ALL])
    open var contact: Contact = Contact()

    @Column(columnDefinition = "VARCHAR")
    open var note: String = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "organizsations")
    open var persons = mutableSetOf<Person>()

    @Column
    open var intern: Boolean = false

    @Column(columnDefinition = "boolean default true")
    open var archived = false

    constructor()

    constructor(contact: Contact) : this("", contact, false)

    constructor(name: String, contact: Contact) : this(name, contact, false)

    constructor(name: String, contact: Contact, intern: Boolean) {
        this.name = name
        this.contact = contact
        this.intern = intern
    }
}
