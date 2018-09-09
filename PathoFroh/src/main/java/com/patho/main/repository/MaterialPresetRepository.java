package com.patho.main.repository;

import com.patho.main.model.MaterialPreset;
import com.patho.main.repository.service.MaterialPresetRepositoryCustom;

public interface MaterialPresetRepository
		extends BaseRepository<MaterialPreset, Long>, MaterialPresetRepositoryCustom {

}
