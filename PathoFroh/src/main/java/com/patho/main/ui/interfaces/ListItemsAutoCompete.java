package com.patho.main.ui.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.patho.main.model.ListItem;

public interface ListItemsAutoCompete {

	public default List<String> getListsuggestions(String input) {
		ArrayList<String> result = new ArrayList<String>();

		for (ListItem items : getPredefinedListItems()) {
			if (items.getValue().toLowerCase().startsWith(input.toLowerCase()))
				result.add(items.getValue());
		}

		return result;
	}

	public List<ListItem> getPredefinedListItems();
}
