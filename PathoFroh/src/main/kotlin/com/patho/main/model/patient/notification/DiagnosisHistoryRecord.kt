package com.patho.main.model.patient.notification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.patho.main.model.patient.DiagnosisRevision
import java.io.Serializable

/**
 * Class for one reportIntent and its reports
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class DiagnosisHistoryRecord : Serializable {

    constructor()

    constructor(diagnosis: DiagnosisRevision) {
        diagnosisID = diagnosis.id
    }

    open var diagnosisID: Long = 0

    open var diagnosisPresent = true

    open var data: MutableList<HistoryEntry> = mutableListOf<HistoryEntry>()
}