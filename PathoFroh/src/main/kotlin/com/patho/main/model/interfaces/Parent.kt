package com.patho.main.model.interfaces

/**
 * Interface for every object of the task tree (Task->Sample->Block->Staining).
 */
public interface Parent<T> : PatientAccessible, TaskAccessible {
    var parent: T?
}
