package com.patho.main.repository.jpa;

import com.patho.main.model.ListItem;
import com.patho.main.repository.jpa.custom.ListItemRepositoryCustom;

import java.util.List;

public interface ListItemRepository extends BaseRepository<ListItem, Long>, ListItemRepositoryCustom {
    public List<ListItem> findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList list, boolean archived);
}
