package com.patho.main.util.ui.jsfcomponents

import com.patho.main.model.patient.Task

/**
 *  Interface for case history component (materialSelectComponent.xhtml)
 */
interface ICaseHistoryComponent {

    /**
     * List of samples
     */
    var task: Task

    /**
     * Data for the case history select overlay
     */
    val caseHistory: ICaseHistorySelectOverlay

    /**
     * Save function
     */
    fun save(task: Task, resourcesKey: String, vararg arr: Any)

    /**
     * Update case history function
     */
    fun updateCaseHistoryWithName(task: Task, caseHistory: String, resourcesKey: String, vararg arr: Any)
}