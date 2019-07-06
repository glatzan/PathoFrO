package com.patho.main.repository.service;

import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.miscellaneous.BioBank;

import java.util.Optional;

public interface BioBankRepositoryCustom {

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id);

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id, boolean loadTask, boolean loadPDFs);

	public Optional<BioBank> findOptionalByTaskAndInitialize(Task task);

	public Optional<BioBank> findOptionalByTaskAndInitialize(Task task, boolean loadTask, boolean loadPDFs);
}
