package com.patho.main.repository.service;

import java.util.List;

import com.patho.main.model.MaterialPreset;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype.StainingType;

public interface MaterialPresetRepositoryCustom {
	public List<MaterialPreset> findAll(boolean loadStainings);

	public List<MaterialPreset> findAllByName(String name, boolean loadStainings);

	/**
	 * 
	 * @param type
	 * @param ignoreArchived
	 * @return
	 */
	public List<StainingPrototype> findAllIgnoreArchivedOrderByPriorityCountDesc(StainingType type,
			boolean loadStainings, boolean irgnoreArchived);
}
