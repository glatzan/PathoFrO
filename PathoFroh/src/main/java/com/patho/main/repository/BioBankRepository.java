package com.patho.main.repository;

import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.miscellaneous.BioBank;
import com.patho.main.repository.service.BioBankRepositoryCustom;

import java.util.Optional;

public interface BioBankRepository extends BaseRepository<BioBank, Long>, BioBankRepositoryCustom {

	public Optional<BioBank> findOptionalByTask(Task task);
	
}
