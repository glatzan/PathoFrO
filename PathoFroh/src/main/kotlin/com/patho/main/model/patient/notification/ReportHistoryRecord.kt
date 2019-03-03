package com.patho.main.model.patient.notification

import com.patho.main.model.json.JsonType
import com.patho.main.model.patient.DiagnosisRevision
import java.time.Instant

/**
 * Class for one diagnosis and its reports
 */
open class ReportHistoryRecord : JsonType<ReportHistoryRecord> {

    constructor()

    constructor(diagnosis: DiagnosisRevision) {
        diagnosisID = diagnosis.id
    }

    open var diagnosisID: Long = 0

    open var diagnosisPresent = true

    open var data: MutableList<ReportData> = mutableListOf<ReportData>()

    /**
     * Class for a single report
     */
    open class ReportData {
        open var failed: Boolean = false
        open var actionDate: Instant = Instant.now()
        open var contactAddress: String = ""
        open var commentary : String = ""
    }
}