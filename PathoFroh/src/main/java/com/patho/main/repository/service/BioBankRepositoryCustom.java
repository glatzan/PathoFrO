package com.patho.main.repository.service;

import java.util.Optional;

import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.miscellaneous.BioBank;

public interface BioBankRepositoryCustom {

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id);

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id, boolean loadTask, boolean loadPDFs);

	public Optional<BioBank> findOptionalByTaskAndInitialize(Task task);

	public Optional<BioBank> findOptionalByTaskAndInitialize(Task task, boolean loadTask, boolean loadPDFs);
}
