package com.patho.main.util.ui.jsfcomponents

import com.patho.main.model.patient.Patient
import com.patho.main.ui.ListChooser

/**
 * Interface is used with the patientTable.xhtml as dataprovider.
 */
interface IPatientTableDataProvider {
    /**
     * List of patient mapped to ListChooser (for unique ids for primefaces datatable)
     */
    var patientList: MutableList<ListChooser<Patient>>

    /**
     * Selected patient
     */
    var selectedPatient: ListChooser<Patient>?

    /**
     * Function is called if a row is double clicked
     */
    fun onDblSelect()

    /**
     * Function is called on select
     */
    fun onSelect()
}