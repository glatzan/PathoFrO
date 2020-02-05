package com.patho.main.repository.jpa

import com.patho.main.model.MaterialPreset
import com.patho.main.repository.jpa.custom.MaterialPresetRepositoryCustom

interface MaterialPresetRepository : MaterialPresetRepositoryCustom, BaseRepository<MaterialPreset, Long> {
}