package com.patho.main.repository;

import java.util.Optional;

import com.patho.main.model.BioBank;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.service.BioBankRepositoryCustom;

public interface BioBankRepository extends BaseRepository<BioBank, Long>, BioBankRepositoryCustom {

	public Optional<BioBank> findOptionalByTask(Task task);
	
}
