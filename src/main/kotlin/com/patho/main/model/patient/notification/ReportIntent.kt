package com.patho.main.model.patient.notification

import com.patho.main.common.ContactRole
import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ID
import com.patho.main.model.patient.Task
import com.patho.main.model.person.Person
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import org.springframework.data.util.ProxyUtils
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
open class ReportIntent : AbstractPersistable, ID {

    @Id
    @GeneratedValue(generator = "reportintent_sequencegenerator")
    @SequenceGenerator(name = "reportintent_sequencegenerator", sequenceName = "reportintent_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    open var task: Task? = null

    @OneToOne(cascade = [CascadeType.DETACH])
    open var person: Person? = null

    @Enumerated(EnumType.STRING)
    open var role = ContactRole.NONE

    @OrderColumn(name = "position")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "contact", cascade = [CascadeType.ALL])
    open var notifications = mutableListOf<ReportIntentNotification>()

    @Column
    open var active: Boolean = true

    @Column
    open var deleteable: Boolean = true

    constructor()

    constructor(task: Task, person: Person, contactRole: ContactRole = ContactRole.NONE, deleteable: Boolean = true) {
        this.task = task
        this.person = person
        this.role = contactRole
        this.deleteable = deleteable
    }

    /**
     * Custom equals, checks if person and role is the same
     */
    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true
        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as ReportIntent

        if (id == other.id) return true

        // same person with the same role, same object
        if (person == other.person && role == other.role)
            return true

        return false

    }

}
