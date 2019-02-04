package com.patho.main.model.interfaces

import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task

/**
 * Interface for every object of the task tree (Task->Sample->Block->Staining).
 */
public interface Parent<T> {
    val patient: Patient?
    val task: Task?
    var parent: T?
}
