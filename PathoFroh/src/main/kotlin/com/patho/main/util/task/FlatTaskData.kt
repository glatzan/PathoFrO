package com.patho.main.util.task

import com.patho.main.model.patient.Task

class FlatTaskData(val task: Task) {

    var newLineChar = 10.toChar()

    val id = task.id
    val taskID = task.taskID
    val receiptDate = task.receiptDate

    val lastName = task.patient?.person?.lastName
    val firstName = task.patient?.person?.firstName
    val birthday = task.patient?.person?.birthday
    val piz = task.parent?.piz

    val diagnoses = task.diagnosisRevisions.joinToString { it.diagnoses.joinToString { d -> "${d.diagnosis}$newLineChar" } }

    val malign = task.diagnosisRevisions.any { it.diagnoses.any { d -> d.malign } }
}