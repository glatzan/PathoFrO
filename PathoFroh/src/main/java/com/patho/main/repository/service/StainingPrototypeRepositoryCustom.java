package com.patho.main.repository.service;

import java.util.Optional;

import com.patho.main.model.StainingPrototype;

public interface StainingPrototypeRepositoryCustom {
	
	/**
	 * Returns a patient with the given id
	 * 
	 * @param id
	 * @return
	 */
	Optional<StainingPrototype> findOptionalByIdAndInitilize(Long id, boolean initializeBatch);

}
