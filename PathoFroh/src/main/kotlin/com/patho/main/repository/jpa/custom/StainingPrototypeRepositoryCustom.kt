package com.patho.main.repository.jpa.custom

import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.preset.StainingPrototypeType
import java.util.*

interface StainingPrototypeRepositoryCustom {

    fun findOptionalByIdAndInitialize(id: Long): Optional<StainingPrototype>

    fun findAllByTypeIgnoreArchivedOrderByPriorityCountDesc(type: StainingPrototypeType,
                                                            ignoreArchived: Boolean): List<StainingPrototype>
}