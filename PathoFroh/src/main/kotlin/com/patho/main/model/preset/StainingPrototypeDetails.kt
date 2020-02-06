package com.patho.main.model.preset

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.time.LocalDate

/**
 * Details for staining batches
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class StainingPrototypeDetails() : Serializable, Cloneable {

    open var name: String = ""

    /**
     * rabbit/ mouse
     */
    open var host: String = ""

    /**
     * e.g. human
     */
    open var specifity: String = ""

    /**
     * e.g. 2ml
     */
    open var quantityDelivered: String = ""

    open var positiveControl: String = ""

    /**
     * temperature
     */
    open var storage: String = ""

    open var bestBefore: LocalDate? = LocalDate.now()

    open var deliveryDate: LocalDate? = LocalDate.now()

    open var emptyDate: LocalDate? = LocalDate.now()

    /**
     * e.g firm
     */
    open var supplier: String = ""

    open var treatment: String = ""

    open var incubationTime = 0

    open var dilution: String = ""

    open var standardDilution: String = ""

    open var process: String = ""

    open var commentary: String = ""

    public override fun clone(): Any {
        return super.clone()
    }
}