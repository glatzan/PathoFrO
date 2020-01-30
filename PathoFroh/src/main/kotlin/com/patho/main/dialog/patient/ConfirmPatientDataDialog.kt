package com.patho.main.dialog.patient

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.model.patient.Patient
import com.patho.main.util.dialog.event.PatientSelectEvent
import org.springframework.stereotype.Component

@Component()
open class ConfirmPatientDataDialog : AbstractDialog_(Dialog.PATIENT_DATA_CONFIRM){

    lateinit var patient : Patient

    var confirmed : Boolean = false

    open fun initAndPrepareBean(patient: Patient): ConfirmPatientDataDialog {
        if (initBean(patient))
            prepareDialog()
        return this
    }

    open fun initBean(patient: Patient): Boolean {
        this.patient = patient

        confirmed = false

        return super.initBean()
    }

    fun hideDialogAndConfirm() {
        super.hideDialog(PatientSelectEvent(patient))
    }
}