package com.patho.main.repository.jpa

import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType
import com.patho.main.model.preset.MaterialPreset
import com.patho.main.repository.jpa.custom.ListItemRepositoryCustom

interface ListItemRepository :  BaseRepository<ListItem, Long>, ListItemRepositoryCustom {
    fun findByListTypeAndArchivedOrderByIndexInListAsc(list: ListItemType, archived: Boolean): List<ListItem>
}