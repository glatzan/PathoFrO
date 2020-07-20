package com.patho.main.repository.jpa.custom

import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType

interface ListItemRepositoryCustom {
    fun findAll(listType: ListItemType, ignoreArchived: Boolean): List<ListItem>

    fun findAllOrderByIndex(listType: ListItemType, ignoreArchived: Boolean): List<ListItem>
}