package com.patho.main.model.interfaces

import com.patho.main.model.patient.Task

interface IdManuallyAltered : TaskAccessible {
    var idManuallyAltered: Boolean
}