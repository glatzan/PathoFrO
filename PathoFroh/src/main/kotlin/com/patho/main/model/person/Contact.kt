package com.patho.main.model.person

import com.patho.main.model.AbstractPersistable
import com.patho.main.model.interfaces.ID
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
@Audited
open class Contact() : AbstractPersistable() {

    @Id
    @GeneratedValue(generator = "contact_sequencegenerator")
    @SequenceGenerator(name = "contact_sequencegenerator", sequenceName = "contact_sequence")
    @Column(unique = true, nullable = false)
    open override var id: Long = 0

    @Column(columnDefinition = "VARCHAR")
    open var building: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var street: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var town: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var postcode: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var country: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var phone: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var mobile: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var email: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var homepage: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var fax: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var pager: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var addressadditon: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var addressadditon2: String = ""
}