package com.patho.main.repository.jpa;

import com.patho.main.model.preset.StainingPrototype;
import com.patho.main.model.preset.StainingPrototypeType;
import com.patho.main.repository.jpa.custom.StainingPrototypeRepositoryCustom;

import java.util.List;

public interface StainingPrototypeRepository
        extends BaseRepository<StainingPrototype, Long>, StainingPrototypeRepositoryCustom {

    List<StainingPrototype> findAllByOrderByPriorityCountDesc();

    List<StainingPrototype> findAllByTypeOrderByPriorityCountDesc(StainingPrototypeType type);
}
