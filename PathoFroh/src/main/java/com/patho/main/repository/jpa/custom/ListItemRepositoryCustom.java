package com.patho.main.repository.jpa.custom;


import com.patho.main.model.preset.ListItem;
import com.patho.main.model.preset.ListItemType;

import java.util.List;

public interface ListItemRepositoryCustom {
    public List<ListItem> findAll(ListItemType listType, boolean irgnoreArchived);

    public List<ListItem> findAllOrderByIndex(ListItemType listType, boolean irgnoreArchived);
}
