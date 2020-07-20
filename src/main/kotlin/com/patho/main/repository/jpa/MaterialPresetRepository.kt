package com.patho.main.repository.jpa

import com.patho.main.model.preset.MaterialPreset
import com.patho.main.model.preset.StainingPrototype
import com.patho.main.repository.jpa.custom.MaterialPresetRepositoryCustom

interface MaterialPresetRepository : MaterialPresetRepositoryCustom, BaseRepository<MaterialPreset, Long> {
}