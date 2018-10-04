package com.patho.main.common;

import java.util.ArrayList;
import java.util.List;

public enum PredefinedFavouriteList {

	/**
	 * List of tasks, of which slides needed to be stained
	 */
	StainingList(1),
	/**
	 * List of tasks where a diagnosis is pending
	 */
	DiagnosisList(2),
	/**
	 * List of tasks where notification is pending
	 */
	NotificationList(3),
	/**
	 * List of tasks where restaining is pending
	 */
	ReStainingList(4),
	/**
	 * List of tasks where a rediagnosis is pending
	 */
	ReDiagnosisList(5),
	/**
	 * For laboratory, not used
	 */
	StayInStainingList(6),

	/**
	 * For physicians, not used
	 */
	StayInDiagnosisList(7),

	/**
	 * For secretary, not used
	 */
	StayInNotificationList(8),
	
	/**
	 * Lists of tasks for that samples should be shipped to the consultant
	 */
	CouncilSendRequestMTA(9), 
	
	/**
	 * Lists of tasks for that the council request should be send via letter
	 */
	CouncilSendRequestSecretary(10),
	/**
	 * List of tasks where the council request is pending
	 */
	CouncilRequest(11),
	/**
	 * Lists of tasks where a council was requested and all councils are performed
	 */
	CouncilCompleted(12),
	/**
	 * List of tasks with active council requests
	 */
	Council(14), ScannList(50), ScannCompletedList(51), ReturnSampleList(52);

	private final long id;

	PredefinedFavouriteList(final int id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public static List<Long> getIdArr() {
		PredefinedFavouriteList[] entry = PredefinedFavouriteList.values();
		List<Long> ids = new ArrayList<Long>(entry.length);

		for (int i = 0; i < entry.length; i++) {
			ids.add(entry[i].getId());
		}

		return ids;
	}
}
