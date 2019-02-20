package com.patho.main.service

import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.notification.ReportHistoryJson
import com.patho.main.model.patient.notification.ReportTransmitter
import com.patho.main.model.patient.notification.ReportTransmitterNotification
import org.springframework.stereotype.Service

@Service()
open class ReportTransmitterService {

    /**
     * Returns all notification records for a diagnosis
     */
    open fun getReportHistoryForDiagnosis(reportTransmitter: ReportTransmitter, diagnosis: DiagnosisRevision): ReportHistoryJson.HistoryRecord {
        return reportTransmitter.history.records.filter { p -> p.diagnosisID == diagnosis.id }.firstOrNull()
                ?: ReportHistoryJson.HistoryRecord(diagnosis)
    }

    /**
     * Returns report data for a specific type and diagnosis
     */
    open fun getReportDataForDiagnosisAndType(reportTransmitter: ReportTransmitter, diagnosis: DiagnosisRevision, type: ReportTransmitterNotification.NotificationTyp): List<ReportHistoryJson.HistoryRecord.ReportData> {
        return getReportDataForType(getReportHistoryForDiagnosis(reportTransmitter, diagnosis), type)
    }

    /**
     * Returns the reportdata of a specific type
     */
    open fun getReportDataForType(historyRecord: ReportHistoryJson.HistoryRecord, type: ReportTransmitterNotification.NotificationTyp): List<ReportHistoryJson.HistoryRecord.ReportData> {
        return historyRecord.data.filter { p -> p.type == type }
    }

}