package com.patho.main.dialog.diagnosis

import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractDialog_
import com.patho.main.model.patient.Diagnosis
import com.patho.main.service.DiagnosisService
import com.patho.main.util.event.dialog.TaskReloadEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class CopyHistologicalRecordDialog @Autowired constructor(
        private val diagnosisService: DiagnosisService) : AbstractDialog_(Dialog.DIAGNOSIS_RECORD_OVERWRITE) {

    open lateinit var diagnosis: Diagnosis

    open fun initAndPrepareBean(diagnosis: Diagnosis): CopyHistologicalRecordDialog {
        if (initBean(diagnosis))
            prepareDialog()
        return this
    }

    open fun initBean(diagnosis: Diagnosis): Boolean {
        this.diagnosis = diagnosis
        return super.initBean()
    }

    open fun copyAndHide(overwrite: Boolean) {
        hideDialog(TaskReloadEvent(diagnosisService.copyHistologicalRecord(diagnosis, overwrite)))
    }

}