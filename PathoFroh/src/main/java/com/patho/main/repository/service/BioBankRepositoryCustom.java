package com.patho.main.repository.service;

import java.util.Optional;

import com.patho.main.model.BioBank;
import com.patho.main.model.patient.Task;

public interface BioBankRepositoryCustom {

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id);

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id, boolean loadTask, boolean loadPDFs);

	public Optional<BioBank> findOptionalByTaskAndInitialize(Task task);

	public Optional<BioBank> findOptionalByTaskAndInitialize(Task task, boolean loadTask, boolean loadPDFs);
}
