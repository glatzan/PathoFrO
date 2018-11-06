package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype.StainingType;
import com.patho.main.repository.service.StainingPrototypeRepositoryCustom;

public interface StainingPrototypeRepository
		extends BaseRepository<StainingPrototype, Long>, StainingPrototypeRepositoryCustom {

	List<StainingPrototype> findAllByOrderByPriorityCountDesc();

	List<StainingPrototype> findAllByTypeOrderByPriorityCountDesc(StainingType type);

}
