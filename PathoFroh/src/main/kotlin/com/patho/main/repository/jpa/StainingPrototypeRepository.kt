package com.patho.main.repository.jpa

import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.preset.StainingPrototypeType
import com.patho.main.repository.jpa.custom.StainingPrototypeRepositoryCustom

interface StainingPrototypeRepository : BaseRepository<StainingPrototype, Long>, StainingPrototypeRepositoryCustom {

    fun findAllByOrderByPriorityCountDesc(): List<StainingPrototype>

    fun findAllByTypeOrderByPriorityCountDesc(type: StainingPrototypeType): List<StainingPrototype>

    fun findAllByOrderByIndexInListAsc() : List<StainingPrototype>
}