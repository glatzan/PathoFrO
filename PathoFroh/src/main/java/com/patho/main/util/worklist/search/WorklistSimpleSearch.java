package com.patho.main.util.worklist.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.common.Month;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.patient.Patient;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.service.PatientRepositoryCustom.FindCriterion;
import com.patho.main.util.helper.TimeUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class WorklistSimpleSearch extends AbstractWorklistSearch {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	private PredefinedFavouriteList[] lists;

	private PredefinedFavouriteList[] selectedLists;

	private boolean newPatients;

	private Date day;

	private Date searchFrom;

	private Date searchTo;

	private Month searchMonth;

	private int year;

	private Map<String, Integer> years;

	private FindCriterion filterIndex;

	private SimpleSearchOption searchIndex;

	public WorklistSimpleSearch() {
		this(null);
	}

	public WorklistSimpleSearch(SimpleSearchOption simpleSearchOption) {
		setLists(PredefinedFavouriteList.values());

		setSearchIndex(simpleSearchOption);
		// checking if null was set
		if (getSearchIndex() == null)
			setSearchIndex(SimpleSearchOption.STAINING_LIST);

		if (getFilterIndex() == null)
			setFilterIndex(FindCriterion.TaskCreated);

		// date for day
		setDay(new Date(System.currentTimeMillis()));

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(System.currentTimeMillis()));

		// date for month in year
		setSearchMonth(Month.getMonthByNumber(cal.get(Calendar.MONTH)));
		setYear(cal.get(Calendar.YEAR));

		// adding 30 years for date for month in year
		setYears(new TreeMap<String, Integer>());
		for (int i = 0; i < 30; i++) {
			getYears().put(Integer.toString(year - i), year - i);
		}

		// set search form to
		setSearchTo(new Date(System.currentTimeMillis()));
		cal.add(Calendar.DAY_OF_MONTH, -1);
		setSearchFrom(cal.getTime());

		updateSearchIndex();
	}

	/**
	 * Sets favorite lists if the search options has associated favorite lists
	 */
	public void updateSearchIndex() {
		setSelectedLists(getSearchIndex().getLists());
		setNewPatients(getSearchIndex().isNewPatient());
	}

	/**
	 * Returns false if any pre configured list is selected. All Filter options will
	 * be disabled
	 * 
	 * @return
	 */
	public boolean isSearchForLists() {
		return getSearchIndex().isPredefinedList();
	}

	/**
	 * Returns a lists of patients
	 */
	@Override
	public List<Patient> getPatients() {
		logger.debug("Searching current worklist");

		ArrayList<Patient> result = new ArrayList<Patient>();

		Calendar cal = Calendar.getInstance();
		Date currentDate = new Date(System.currentTimeMillis());
		cal.setTime(currentDate);

		switch (getSearchIndex()) {
		case STAINING_LIST:
		case DIAGNOSIS_LIST:
		case NOTIFICATION_LIST:
		case CUSTOM_LIST:

			logger.debug("Staining list selected");

			// getting new stainigs
			if (isNewPatients()) {
				result.addAll(patientRepository.findByDateAndCriterion(FindCriterion.NoTasks,
						TimeUtil.setDayBeginning(cal).getTimeInMillis(), TimeUtil.setDayEnding(cal).getTimeInMillis(),
						true, false, true));
			}

			
			System.out.println("--------" + patientRepository);
			
			if (getSelectedLists() != null && getSelectedLists().length > 0) {
				patientRepository.findAllByFavouriteLists(
						Arrays.asList(getSelectedLists()).stream().map(p -> p.getId()).collect(Collectors.toList()),
						true);
			}

			break;
		case TODAY:
			logger.debug("Today selected");
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex,
					TimeUtil.setDayBeginning(cal).getTimeInMillis(), TimeUtil.setDayEnding(cal).getTimeInMillis(), true,
					false, true));
			break;
		case YESTERDAY:
			logger.debug("Yesterdy selected");
			cal.add(Calendar.DAY_OF_MONTH, -1);
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex,
					TimeUtil.setDayBeginning(cal).getTimeInMillis(), TimeUtil.setDayEnding(cal).getTimeInMillis(), true,
					false, true));
			break;
		case CURRENTWEEK:
			logger.debug("Current week selected");
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex,
					TimeUtil.setWeekBeginning(cal).getTimeInMillis(), TimeUtil.setWeekEnding(cal).getTimeInMillis(),
					true, false, true));
			break;
		case LASTWEEK:
			logger.debug("Last week selected");
			cal.add(Calendar.WEEK_OF_YEAR, -1);
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex,
					TimeUtil.setWeekBeginning(cal).getTimeInMillis(), TimeUtil.setWeekEnding(cal).getTimeInMillis(),
					true, false, true));
			break;
		case CURRENTMONTH:
			logger.debug("Current month selected");
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex,
					TimeUtil.setMonthBeginning(cal).getTimeInMillis(), TimeUtil.setMonthEnding(cal).getTimeInMillis(),
					true, false, true));
			break;
		case LASTMONTH:
			cal.add(Calendar.MONTH, -1);
			logger.debug("Last month selected " + cal);
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex,
					TimeUtil.setMonthBeginning(cal).getTimeInMillis(), TimeUtil.setMonthEnding(cal).getTimeInMillis(),
					true, false, true));
			break;
		case DAY:
			logger.debug("Day selected");
			cal.setTime(getDay());
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex,
					TimeUtil.setDayBeginning(cal).getTimeInMillis(), TimeUtil.setDayEnding(cal).getTimeInMillis(), true,
					false, true));
			break;
		case MONTH:
			logger.debug("Month selected");
			cal.set(Calendar.MONTH, getSearchMonth().getNumber());
			cal.set(Calendar.YEAR, getYear());
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex,
					TimeUtil.setMonthBeginning(cal).getTimeInMillis(), TimeUtil.setMonthEnding(cal).getTimeInMillis(),
					true, false, true));
			break;
		case TIME:
			logger.debug("Time selected");
			cal.setTime(getSearchFrom());
			long fromTime = TimeUtil.setDayBeginning(cal).getTimeInMillis();
			cal.setTime(getSearchTo());
			long toTime = TimeUtil.setDayEnding(cal).getTimeInMillis();
			result.addAll(patientRepository.findByDateAndCriterion(filterIndex, fromTime, toTime, true, false, true));
			break;
		default:
			break;
		}

		return result;
	}

	/**
	 * Selectable predefined sort options
	 * 
	 * @author andi
	 *
	 */
	@Getter
	public enum SimpleSearchOption {

		STAINING_LIST(true, PredefinedFavouriteList.StainingList, PredefinedFavouriteList.StayInStainingList,
				PredefinedFavouriteList.ReStainingList, PredefinedFavouriteList.CouncilSendRequestMTA,
				PredefinedFavouriteList.ScannList), DIAGNOSIS_LIST(false, PredefinedFavouriteList.DiagnosisList,
						PredefinedFavouriteList.ReDiagnosisList, PredefinedFavouriteList.StayInDiagnosisList,
						PredefinedFavouriteList.CouncilCompleted), NOTIFICATION_LIST(false,
								PredefinedFavouriteList.NotificationList,
								PredefinedFavouriteList.StayInNotificationList,
								PredefinedFavouriteList.CouncilSendRequestSecretary), CUSTOM_LIST, EMPTY_LIST, TODAY, YESTERDAY, CURRENTWEEK, LASTWEEK, CURRENTMONTH, LASTMONTH, DAY, MONTH, TIME,;

		private final PredefinedFavouriteList[] lists;
		private final boolean newPatient;
		private final boolean predefinedList;

		SimpleSearchOption() {
			this(false);
		}

		SimpleSearchOption(boolean newPatient, PredefinedFavouriteList... lists) {
			this.newPatient = newPatient;
			this.lists = lists;
			this.predefinedList = lists == null || lists.length == 0 ? false : true;
		}
	}
}
