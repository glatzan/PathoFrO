package com.patho.main.util.ui.jsfcomponents

import com.patho.main.model.Signature
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Task

interface IDiagnosisViewFunction {

    fun updatePhysiciansSignature(task: Task, signature: Signature, resourcesKey: String, vararg arr: Any)

    fun beginDiagnosisAmendment(diagnosisRevision: DiagnosisRevision)

    fun endDiagnosisAmendment(diagnosisRevision: DiagnosisRevision)

    fun save(task: Task, resourcesKey: String, vararg arr: Any)
}