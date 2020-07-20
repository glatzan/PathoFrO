package com.patho.main.model.patient.notification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.time.Instant

/**
 * Class for a single report
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class HistoryEntry() : Serializable {
    open var failed: Boolean = false
    open var actionDate: Instant = Instant.now()
    open var contactAddress: String = ""
    open var commentary: String = ""
}