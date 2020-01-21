package com.patho.main.repository.jpa;

import com.patho.main.model.MaterialPreset;
import com.patho.main.repository.jpa.custom.MaterialPresetRepositoryCustom;

public interface MaterialPresetRepository
        extends BaseRepository<MaterialPreset, Long>, MaterialPresetRepositoryCustom {

}
