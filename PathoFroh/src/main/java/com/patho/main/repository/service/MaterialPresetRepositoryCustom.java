package com.patho.main.repository.service;

import java.util.List;

import com.patho.main.model.MaterialPreset;

public interface MaterialPresetRepositoryCustom {
	public List<MaterialPreset> findAll(boolean loadStainings);
}
