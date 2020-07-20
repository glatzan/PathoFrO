package com.patho.main.util.task

import com.patho.main.model.patient.Task
import java.io.Serializable

class FlatTaskData(val task: Task) : Serializable {

    var newLineChar = 10.toChar()

    val id = task.id
    val ed_TaskID = task.taskID
    val ed_ReceiptDate = task.receiptDate

    val ed_LastName = task.patient?.person?.lastName
    val ed_FirstName = task.patient?.person?.firstName
    val ed_Birthday = task.patient?.person?.birthday
    val ed_Piz = task.parent?.piz

    val e_Diagnoses = task.diagnosisRevisions.joinToString { it.diagnoses.joinToString { d -> "${d.diagnosis}$newLineChar" } }

    val e_Malign = task.diagnosisRevisions.any { it.diagnoses.any { d -> d.malign } }
}