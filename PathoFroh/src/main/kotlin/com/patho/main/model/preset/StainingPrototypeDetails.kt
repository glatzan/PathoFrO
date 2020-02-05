package com.patho.main.model.preset

import com.patho.main.model.AbstractPersistable
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
open class StainingPrototypeDetails : AbstractPersistable {

    @Id
    @GeneratedValue(generator = "stainingPrototypeDetails_sequencegenerator")
    @SequenceGenerator(name = "stainingPrototypeDetails_sequencegenerator", sequenceName = "stainingPrototypeDetails_sequence")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @Column(columnDefinition = "VARCHAR")
    open var name: String = ""

    /**
     * Parent
     */
    @ManyToOne(fetch = FetchType.LAZY)
    open var staining: StainingPrototype

    /**
     * rabbit/ mouse
     */
    @Column(columnDefinition = "VARCHAR")
    open var host: String = ""

    /**
     * e.g. human
     */
    @Column(columnDefinition = "VARCHAR")
    open var specifity: String = ""

    /**
     * e.g. 2ml
     */
    @Column(columnDefinition = "VARCHAR")
    open var quantityDelivered: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var positiveControl: String = ""

    /**
     * temperature
     */
    @Column(columnDefinition = "VARCHAR")
    open var storage: String = ""

    @Column(columnDefinition = "VARCHAR")
    @Type(type = "date")
    open var bestBefore: Date = ""

    @Column(columnDefinition = "VARCHAR")
    @Type(type = "date")
    open var deliveryDate: Date? = ""

    @Column(columnDefinition = "VARCHAR")
    @Type(type = "date")
    open var emptyDate: Date? = ""

    /**
     * e.g firm
     */
    @Column(columnDefinition = "VARCHAR")
    open var supplier: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var treatment: String = ""

    @Column
    open var incubationTime = 0

    @Column(columnDefinition = "VARCHAR")
    open var dilution: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var standardDilution: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var process: String = ""

    @Column(columnDefinition = "VARCHAR")
    open var commentary: String = ""


    constructor(prototpye : StainingPrototype){
        this.staining = prototpye
    }
}