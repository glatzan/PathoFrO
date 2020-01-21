package com.patho.main.util.status

import com.patho.main.util.status.ExtendedNotificationStatus

/**
 * Interface for OverlayNotificationStatusForReportIntent.xhtml, the original
 * com.patho.main.util.status.ExtendedNotificationStatus.ReportNotificationIntentStatus inner
 * class can not be used in jsf components
 *
 * Is implemented by @see com.patho.main.util.status.ExtendedNotificationStatus.ReportNotificationIntentStatus.ReportIntentStatus
 * Display Element: OverlayNotificationStatusForReportIntent.xhtml (displays ReportIntentStatus -> DiagnosisRevisionStatus)
 */
interface IExtendedNotificationStatusForReportIntent {
    val diagnoses: List<ExtendedNotificationStatus.ReportNotificationIntentStatus.ReportIntentStatus.DiagnosisRevisionStatus>
}