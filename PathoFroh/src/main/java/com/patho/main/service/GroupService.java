package com.patho.main.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.View;
import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoUser;
import com.patho.main.util.worklist.search.WorklistSimpleSearch.SimpleSearchOption;

public class GroupService {
	/**
	 * Copies crucial settings from group settings to user settings
	 * 
	 * @param user
	 * @param group
	 */
	public static void copyGroupSettings(HistoUser user, HistoGroup group, boolean overwrite) {

		user.setArchived(group.isUserDeactivated());

		user.getSettings().setAvailableViews(new ArrayList<View>(group.getSettings().getAvailableViews()));
		user.getSettings().setDefaultView(group.getSettings().getDefaultView());
		user.getSettings().setStartView(group.getSettings().getStartView());

		if (user.getSettings().getInputFieldColor() == null || overwrite)
			user.getSettings().setInputFieldColor(group.getSettings().getInputFieldColor());

		if (user.getSettings().getInputFieldFontColor() == null || overwrite)
			user.getSettings().setInputFieldFontColor(group.getSettings().getInputFieldFontColor());

		user.getSettings()
				.setAvailableWorklists(new ArrayList<SimpleSearchOption>(group.getSettings().getAvailableWorklists()));

		user.getSettings().setWorklistToLoad(group.getSettings().getWorklistToLoad());

		user.getSettings().setWorklistSortOrder(group.getSettings().getWorklistSortOrder());

		user.getSettings().setWorklistSortOrderAsc(group.getSettings().isWorklistSortOrderAsc());

		user.getSettings().setWorklistHideNoneActiveTasks(group.getSettings().isWorklistHideNoneActiveTasks());

		user.getSettings().setWorklistAutoUpdate(group.getSettings().isWorklistAutoUpdate());

		user.getSettings().setAlternatePatientAddMode(group.getSettings().isAlternatePatientAddMode());

		user.getSettings().setAddTaskWithSingelClick(group.getSettings().isAddTaskWithSingelClick());

	}

	/**
	 * Copies only view settings to the user settings
	 * 
	 * @param user
	 * @param group
	 * @param overwrite
	 */
	public static void copyUpdatedGroupSettings(HistoUser user, HistoGroup group) {

		user.getSettings().setAvailableViews(new ArrayList<View>(group.getSettings().getAvailableViews()));
		user.getSettings().setDefaultView(group.getSettings().getDefaultView());
		user.getSettings().setStartView(group.getSettings().getStartView());

		user.getSettings()
				.setAvailableWorklists(new ArrayList<SimpleSearchOption>(group.getSettings().getAvailableWorklists()));

		user.getSettings().setWorklistToLoad(group.getSettings().getWorklistToLoad());
	}

}
