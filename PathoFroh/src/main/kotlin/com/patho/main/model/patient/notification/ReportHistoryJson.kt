package com.patho.main.model.patient.notification

import com.patho.main.model.json.JsonType
import com.patho.main.model.patient.DiagnosisRevision
import java.time.Instant

/**
 * Container class for a history list for all diagnoses
 */
open class ReportHistoryJson : JsonType<ReportTransmitterNotificationDataJson>() {

    open var records: MutableList<HistoryRecord> = mutableListOf<HistoryRecord>()

    /**
     * Class for one diagnosis and its reports
     */
    open class HistoryRecord {

        constructor()

        constructor(diagnosis: DiagnosisRevision) {
            diagnosisID = diagnosis.id
        }

        open var diagnosisID: Long = 0

        open var data: MutableList<ReportData> = mutableListOf<ReportData>()

        /**
         * Class for a single report
         */
        open class ReportData {
            open var failed: Boolean = false
            open var actionDate: Instant = Instant.now()
            open var type: ReportTransmitterNotification.NotificationTyp = ReportTransmitterNotification.NotificationTyp.NONE
            open var contactAddress: String = ""
        }
    }
}