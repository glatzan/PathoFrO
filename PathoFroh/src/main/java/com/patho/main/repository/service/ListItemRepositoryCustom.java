package com.patho.main.repository.service;

import com.patho.main.model.ListItem;

import java.util.List;

public interface ListItemRepositoryCustom {
	public List<ListItem> findAll(ListItem.StaticList listType, boolean irgnoreArchived);

	public List<ListItem> findAllOrderByIndex(ListItem.StaticList listType, boolean irgnoreArchived);
}
