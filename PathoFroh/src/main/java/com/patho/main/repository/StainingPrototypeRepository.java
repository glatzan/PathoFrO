package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype.StainingType;

public interface StainingPrototypeRepository extends BaseRepository<StainingPrototype, Long> {
	
	public List<StainingPrototype> findAllByOrderByIndexInListAsc();
	
	public List<StainingPrototype> findAllByTypeOrderByIndexInListAsc(StainingType type);
	
}
