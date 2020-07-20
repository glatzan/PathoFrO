package com.patho.main.model.person

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.FullName
import org.hibernate.annotations.*
import org.hibernate.envers.Audited
import java.time.LocalDate
import javax.persistence.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OrderBy


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Audited
@SelectBeforeUpdate(true)
open class Person : AbstractPersistable, FullName {

    @Id
    @SequenceGenerator(name = "person_sequencegenerator", sequenceName = "person_sequence")
    @GeneratedValue(generator = "person_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    @Enumerated(EnumType.ORDINAL)
    open override var gender = Gender.UNKNOWN

    @Column(columnDefinition = "VARCHAR")
    open override var title = ""

    @Column(columnDefinition = "VARCHAR")
    open override var lastName: String = ""

    @Column(columnDefinition = "VARCHAR")
    open override var firstName: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var birthName: String = ""

    @Column
    open var birthday: LocalDate? = null

    @Column(columnDefinition = "VARCHAR")
    open var language: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var note: String = ""

    @OneToOne(cascade = [CascadeType.ALL])
    open var contact: Contact = Contact()

    @Column
    protected open var archived: Boolean = false

    /**
     * Default address for notification
     */
    @OneToOne
    open var defaultAddress: Organization? = null

    /**
     * If true the persons data will no be updated with backend data, default true
     */
    @Column(columnDefinition = "boolean default true")
    open var autoUpdate = true

    @ManyToMany
    @OrderBy("id ASC")
    @Fetch(value = FetchMode.SUBSELECT)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "person_organization", joinColumns = [JoinColumn(name = "person_id")], inverseJoinColumns = [JoinColumn(name = "organization_id")])
    open var organizsations = mutableSetOf<Organization>()

    constructor()

    constructor(name: String) : this(name, Contact())

    constructor(contact: Contact) : this("", contact)

    constructor(name: String, contact: Contact) {
        this.lastName = name
        this.contact = contact
    }

    enum class Gender {
        MALE, FEMALE, UNKNOWN
    }
}