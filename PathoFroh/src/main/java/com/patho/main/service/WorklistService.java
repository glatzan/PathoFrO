package com.patho.main.service;

import org.springframework.stereotype.Service;

import com.patho.main.model.user.HistoSettings;
import com.patho.main.util.worklist.Worklist;
import com.patho.main.util.worklist.search.AbstractWorklistSearch;

@Service
public class WorklistService {

	public Worklist getWorklist(String name, AbstractWorklistSearch worklistSearch, HistoSettings settings) {
		Worklist worklist = new Worklist(name, worklistSearch, settings.getWorklistHideNoneActiveTasks(),
				settings.getWorklistSortOrder(), settings.getWorklistAutoUpdate(), false,
				settings.getWorklistSortOrderAsc());
		
		return worklist;
	}

}
