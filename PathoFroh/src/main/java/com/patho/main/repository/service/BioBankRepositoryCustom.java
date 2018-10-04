package com.patho.main.repository.service;

import java.util.Optional;

import com.patho.main.model.BioBank;

public interface BioBankRepositoryCustom {
	
	public Optional<BioBank> findOptionalByIdAndInitialize(Long id);

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id, boolean loadTask, boolean loadPDFs);
}
