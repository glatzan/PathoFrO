package com.patho.main.repository.jpa;

import com.patho.main.model.user.HistoGroup;
import com.patho.main.repository.jpa.custom.GroupRepositoryCustom;

import java.util.Optional;

public interface GroupRepository extends BaseRepository<HistoGroup, Long>, GroupRepositoryCustom {

    Optional<HistoGroup> findOptionalById(Long id);

    Optional<HistoGroup> findOptionalByName(String name);

}
