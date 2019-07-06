package com.patho.main.repository;

import java.util.Optional;

import com.patho.main.model.patient.Task;
import com.patho.main.repository.custom.TaskRepositoryCustom;

public interface TaskRepository extends BaseRepository<Task, Long>, TaskRepositoryCustom {
	public Optional<Task> findOptionalById(Long id);
}
