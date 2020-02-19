package com.patho.main.util.ui.jsfcomponents

import com.patho.main.model.patient.Diagnosis
import com.patho.main.model.patient.Sample
import com.patho.main.model.preset.DiagnosisPreset
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.MaterialPreset

/**
 * Interface for material select overlay component (materialSelectOverlay.xhtml)
 */
interface IDiagnosisSelectOverlay {

    /**
     * Selected Diagnosis
     */
    var selectedDiagnosis: DiagnosisPreset?

    /**
     * List of all Diagnoses
     */
    val diagnoses: List<DiagnosisPreset>

    /**
     * Filter string for search
     */
    var filter: String

    /**
     * Function is called on select
     */
    fun onSelect() {}

    /**
     * Function is called on show
     */
    fun onShow() {}
}