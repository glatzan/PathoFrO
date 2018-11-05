package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.ListItem;
import com.patho.main.repository.service.ListItemRepositoryCustom;

public interface ListItemRepository extends BaseRepository<ListItem, Long>, ListItemRepositoryCustom {
	public List<ListItem> findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList list, boolean archived);
}
