package com.patho.main.util.ui.selector

import com.patho.main.model.patient.notification.ReportIntent

class ReportIntentSelector : UISelector<ReportIntent> {
    var deleteAble: Boolean = false

    constructor(reportIntent: ReportIntent) : super(reportIntent) {
    }
}
