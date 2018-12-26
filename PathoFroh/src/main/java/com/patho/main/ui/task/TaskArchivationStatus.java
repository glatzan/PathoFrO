package com.patho.main.ui.task;

import java.util.List;

import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskArchivationStatus {

	private boolean archiveAble;

	private Task task;

	private boolean stainingNeeded;
	private boolean notificationNeeded;
	private boolean diangosisNeeded;

	private boolean councilNotCompleted;

	private List<FavouriteList> blockingLists;

	public TaskArchivationStatus(Task task) {
		this.task = task;
	}
}
