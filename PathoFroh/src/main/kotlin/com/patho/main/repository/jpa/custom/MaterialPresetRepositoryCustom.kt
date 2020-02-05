package com.patho.main.repository.jpa.custom

import com.patho.main.model.preset.MaterialPreset


interface MaterialPresetRepositoryCustom {

    fun findAll(loadStainings: Boolean): List<MaterialPreset>

    fun findAllByName(name: String, loadStainings: Boolean): List<MaterialPreset>

    fun findAllOrderByPriorityCountDesc(loadStainings: Boolean,
                                        irgnoreArchived: Boolean): List<MaterialPreset>
}