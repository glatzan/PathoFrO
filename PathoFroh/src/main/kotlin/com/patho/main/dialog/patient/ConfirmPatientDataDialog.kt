package com.patho.main.dialog.patient

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.model.patient.Patient
import com.patho.main.util.dialog.event.ConfirmEvent
import com.patho.main.util.dialog.event.PatientSelectEvent
import org.primefaces.event.SelectEvent
import org.springframework.stereotype.Component

@Component()
open class ConfirmPatientDataDialog : AbstractDialog_(Dialog.PATIENT_DATA_CONFIRM) {

    lateinit var patient: Patient

    var confirmed: Boolean = false

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

    /**
     * Is called on confirm dialog return
     */
    fun onConfirmDialogReturn(event: SelectEvent) {
        val obj = event.getObject()

        if (obj != null && obj is ConfirmEvent) {
            return if (obj.obj) {
                hideDialog(PatientSelectEvent(patient))
            } else
                hideDialog()
        }

        return hideDialog()
    }
}