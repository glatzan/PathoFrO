package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.ListItem;

public interface ListItemRepository extends BaseRepository<ListItem, Long> {
	public List<ListItem> findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList list, boolean archived);
}
