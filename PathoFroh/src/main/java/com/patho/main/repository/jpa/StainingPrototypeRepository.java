package com.patho.main.repository.jpa;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype.StainingType;
import com.patho.main.repository.jpa.custom.StainingPrototypeRepositoryCustom;

import java.util.List;

public interface StainingPrototypeRepository
        extends BaseRepository<StainingPrototype, Long>, StainingPrototypeRepositoryCustom {

    List<StainingPrototype> findAllByOrderByPriorityCountDesc();

    List<StainingPrototype> findAllByTypeOrderByPriorityCountDesc(StainingType type);
}
