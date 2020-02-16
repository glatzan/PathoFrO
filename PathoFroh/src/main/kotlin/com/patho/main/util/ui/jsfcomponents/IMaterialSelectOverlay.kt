package com.patho.main.util.ui.jsfcomponents

import com.patho.main.model.patient.Sample
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.MaterialPreset

/**
 * Interface for material select overlay component (materialSelectOverlay.xhtml)
 */
interface IMaterialSelectOverlay {

    /**
     * List of all MaterialPreset
     */
    var selectedMaterial: MaterialPreset?

    /**
     * Selected MaterialPreset
     */
    val presets: List<MaterialPreset>

    /**
     * Filter string for search
     */
    var filter: String

    /**
     * Parent sample for the material
     */
    var parentSample : Sample?

    /**
     * Function is called on select
     */
    fun onSelect() {}

    /**
     * Function is called on show
     */
    fun onShow() {}
}