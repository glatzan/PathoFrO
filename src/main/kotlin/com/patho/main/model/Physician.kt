package com.patho.main.model

import com.patho.main.common.ContactRole
import com.patho.main.model.person.Person
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
open class Physician : AbstractPersistable {

    @Id
    @SequenceGenerator(name = "physician_sequencegenerator", sequenceName = "physician_sequence")
    @GeneratedValue(generator = "physician_sequencegenerator")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Version
    open var version: Long = 0

    /**
     * Clinic internal title
     */
    @Column(columnDefinition = "VARCHAR")
    open var clinicRole: String = ""

    /**
     * Number of the employee
     */
    @Column(columnDefinition = "VARCHAR")
    open var employeeNumber: String = ""

    /**
     * Login name of the physician
     */
    @Column(columnDefinition = "VARCHAR")
    open var uid: String = ""

    /**
     * List of all contactRoles
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Cascade(value = [org.hibernate.annotations.CascadeType.ALL])
    open var associatedRoles: MutableSet<ContactRole> = mutableSetOf()

    /**
     * Person data of the physician
     */
    @OneToOne(cascade = [CascadeType.ALL])
    open var person: Person = Person()

    /**
     * Transitory, if fetched from ldap this variable contains the dn objects name.
     */
    @Transient
    open var dnObjectName: String = ""

    /**
     * If true this object is archived
     */
    @Column
    open var archived: Boolean = false

    /**
     * On every selection of the physician, this number will be increased. The
     * physicians can be ordered according to this value. So often used physicians
     * will be displayed first.
     */
    @Column
    open var priorityCount: Int = 0

    constructor()

    constructor(id: Long) {
        this.id = id
    }

    constructor(person: Person) {
        this.person = person
    }

    /**
     * List of contact Role as an array, used by gui
     */
    open var associatedRolesAsArray
        @Transient
        get() = associatedRoles.toTypedArray()
        @Transient
        set(value) {
            associatedRoles = value.toMutableSet()
        }

    /**
     * Returns true if the physician has the given role
     */
    @Transient
    open fun hasAssociateRole(role: ContactRole): Boolean = associatedRoles.any { p -> p == role }

    /**
     * Returns true if no associatedRoles are set
     */
    @Transient
    open fun hasNoAssociateRole(): Boolean = associatedRoles.isEmpty()

    /**
     * Adds an role to the physician
     */
    @Transient
    open fun addAssociatedRole(role: ContactRole) {
        associatedRoles.add(role)
    }

    /**
     * Returns true if the physician is associated with an interal organization
     */
    @Transient
    open fun isClinicEmployee() = person.organizsations.any { p -> p.intern }

}