package com.patho.main.repository.service;

import java.util.List;

import com.patho.main.model.ListItem;

public interface ListItemRepositoryCustom {
	public List<ListItem> findAll(ListItem.StaticList listType, boolean irgnoreArchived);
}
