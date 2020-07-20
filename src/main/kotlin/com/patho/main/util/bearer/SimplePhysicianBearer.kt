package com.patho.main.util.bearer

import com.patho.main.model.Physician
import com.patho.main.model.patient.Task


class SimplePhysicianBearer(override var id: Long, val physician: Physician, val task: Task) : AbstactBearer(id) {
    val contactOfTask: Boolean = task.contacts.any { p -> p.person == physician.person }
}