package com.patho.main.repository.service;

import com.patho.main.model.MaterialPreset;

import java.util.List;

public interface MaterialPresetRepositoryCustom {
	public List<MaterialPreset> findAll(boolean loadStainings);

	public List<MaterialPreset> findAllByName(String name, boolean loadStainings);

	public List<MaterialPreset> findAllIgnoreArchivedOrderByPriorityCountDesc(boolean loadStainings,
			boolean irgnoreArchived);
}
