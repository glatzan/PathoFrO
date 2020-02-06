package com.patho.main.repository.jpa.custom;

import com.patho.main.model.preset.StainingPrototype;
import com.patho.main.model.preset.StainingPrototypeType;

import java.util.List;
import java.util.Optional;

public interface StainingPrototypeRepositoryCustom {

    /**
     * Returns a patient with the given id
     *
     * @param id
     * @return
     */
    Optional<StainingPrototype> findOptionalByIdAndInitilize(Long id, boolean initializeBatch);

    /**
     * @return
     */
    List<StainingPrototype> findAllByTypeIgnoreArchivedOrderByPriorityCountDesc(StainingPrototypeType type,
                                                                                boolean initializeBatch, boolean irgnoreArchived);
}
