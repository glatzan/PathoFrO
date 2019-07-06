package com.patho.main.service;

import com.patho.main.model.user.HistoSettings;
import com.patho.main.util.search.settings.SearchSettings;
import com.patho.main.util.worklist.Worklist;
import org.springframework.stereotype.Service;

@Service
public class WorklistService {

	public Worklist getWorklist(String name, SearchSettings worklistSearch, HistoSettings settings) {
		Worklist worklist = new Worklist(name, worklistSearch, settings.getWorklistHideNoneActiveTasks(),
				settings.getWorklistSortOrder(), settings.getWorklistAutoUpdate(), false,
				settings.getWorklistSortOrderAsc());
		
		return worklist;
	}

}
