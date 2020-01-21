package com.patho.main.repository.jpa.custom;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype.StainingType;

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
     * @param type
     * @param ignoreArchived
     * @return
     */
    List<StainingPrototype> findAllByTypeIgnoreArchivedOrderByPriorityCountDesc(StainingType type,
                                                                                boolean initializeBatch, boolean irgnoreArchived);
}
