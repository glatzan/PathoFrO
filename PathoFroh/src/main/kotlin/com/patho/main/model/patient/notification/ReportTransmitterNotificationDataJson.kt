package com.patho.main.model.patient.notification

import com.patho.main.model.json.JsonType
import org.hibernate.annotations.TypeDef

open class ReportTransmitterNotificationDataJson : JsonType<ReportTransmitterNotificationDataJson>() {

    /**
     * ID of the diagnosis
     */
    open var diagnosisID: Long? = null
}