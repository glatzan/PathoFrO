package com.patho.main.repository;

import java.util.Optional;

import com.patho.main.model.user.HistoGroup;
import com.patho.main.repository.service.GroupRepositoryCustom;

public interface GroupRepository extends BaseRepository<HistoGroup, Long>, GroupRepositoryCustom {

	Optional<HistoGroup> findOptionalById(Long id);

	Optional<HistoGroup> findOptionalByName(String name);
}
