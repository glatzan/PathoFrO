package com.patho.main.util.ui.jsfcomponents

import com.patho.main.model.patient.Task
import com.patho.main.model.preset.MaterialPreset
import javassist.tools.rmi.Sample

/**
 *  Interface for material select inputs (materialSelectTable.xhtml)
 */
interface IMaterialSelectTable {

    /**
     * List of samples
     */
    var task: Task

    /**
     * Data for the material select overlay
     */
    val material: IMaterialSelectOverlay

    /**
     * Save function
     */
    fun save(task: Task, resourcesKey: String, vararg arr: Any)

    /**
     * Update material function
     */
    fun updateMaterialOfSample(sample: com.patho.main.model.patient.Sample, materialPreset: MaterialPreset?, materialPresetString: String, resourcesKey: String, vararg arr: Any)
}