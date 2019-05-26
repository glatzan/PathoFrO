package com.patho.main.dialog.phase

import com.patho.main.common.Dialog
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task

open class DiagnosisPhaseExitDialog : AbstractPhaseExitDialog(Dialog.DIAGNOSIS_PHASE_EXIT){


    open fun initBean(task: Task, diagnosisRevision: DiagnosisRevision){



        super.initBean(task)
    }
}