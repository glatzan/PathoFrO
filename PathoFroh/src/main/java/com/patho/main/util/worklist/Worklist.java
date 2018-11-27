package com.patho.main.util.worklist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.common.WorklistSortOrder;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoSettings;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.ui.task.TaskInfo;
import com.patho.main.util.helper.TaskUtil;
import com.patho.main.util.worklist.search.AbstractWorklistSearch;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class Worklist {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	/**
	 * Patients
	 */
	private List<Patient> items;

	/**
	 * Sortorder of worklist
	 */
	private WorklistSortOrder worklistSortOrder;

	/**
	 * True if sort should be ascending, if falseF sort will be descending
	 */
	private boolean sortAscending;

	/**
	 * If true, only tasks which are explicitly marked as active are highlighted,
	 * the other tasks will be grayed out
	 */
	private boolean showActiveTasksExplicit;

	/**
	 * If true none active tasks will be shown
	 */
	private boolean showNoneActiveTasks;

	/**
	 * True if auto update of worklist shoul take place
	 */
	private boolean autoUpdate;

	/**
	 * Name of the worklist
	 */
	private String name;

	/**
	 * Update interval if enabled in sec
	 */
	private int udpateInterval = 10;

	/**
	 * Search criteria
	 */
	private AbstractWorklistSearch worklistSearch;

	/**
	 * Selected Patient
	 */
	private Patient selectedPatient;

	/**
	 * Selected Task
	 */
	private Task selectedTask;

	/**
	 * Selected Task info, can persist over task reloads
	 */
	private TaskInfo selectedTaskInfo;

	public Worklist(String name, AbstractWorklistSearch worklistSearch) {
		this(name, worklistSearch, true, WorklistSortOrder.TASK_ID, false);
	}

	public Worklist(String name, AbstractWorklistSearch worklistSearch, HistoSettings settings) {
		this(name, worklistSearch, true, WorklistSortOrder.TASK_ID, false);
	}

	public Worklist(String name, AbstractWorklistSearch worklistSearch, boolean showNoneActiveTasks,
			WorklistSortOrder worklistSortOrder, boolean autoUpdate) {
		this(name, worklistSearch, showNoneActiveTasks, worklistSortOrder, autoUpdate, false, false);
	}

	public Worklist(String name, AbstractWorklistSearch worklistSearch, boolean showNoneActiveTasks,
			WorklistSortOrder worklistSortOrder, boolean autoUpdate, boolean showActiveTasksExplicit,
			boolean sortAscending) {
		this.name = name;
		this.worklistSearch = worklistSearch;

		this.showActiveTasksExplicit = showActiveTasksExplicit;
		this.showNoneActiveTasks = showNoneActiveTasks;
		this.worklistSortOrder = worklistSortOrder;
		this.autoUpdate = autoUpdate;

		this.sortAscending = sortAscending;
		this.items = new ArrayList<Patient>();
	}

	/**
	 * Returns true if patient is present in the worklist
	 * 
	 * @param patient
	 * @return
	 */
	public boolean contains(Patient patient) {
		return getItems().stream().anyMatch(p -> p.equals(patient));
	}

	/**
	 * Returns true if the patient is selected
	 * 
	 * @param patient
	 * @return
	 */
	public boolean isSelected(Patient patient) {
		return patient != null && patient.equals(getSelectedPatient());
	}

	/**
	 * Returns true if the selected task equals the given task
	 * 
	 * @param task
	 * @return
	 */
	public boolean isSelected(Task task) {
		return task != null && task.equals(getSelectedTask());
	}

	/**
	 * REturns true if a patient is selected
	 * 
	 * @return
	 */
	public boolean isPatientSelected() {
		return getSelectedPatient() != null;
	}

	/**
	 * Returns true if a patient and a task is selected
	 * 
	 * @return
	 */
	public boolean isTaskSelected() {
		return isPatientSelected() && getSelectedTask() != null;
	}

	/**
	 * Adds a patient to the worklist, if already in worklist the patient will be
	 * replaced. If the patient is set as selected, this will also be updated.
	 * 
	 * @param patient
	 */
	public void add(Patient patient) {
		add(patient, false);
	}

	/**
	 * Adds a patient to the worklist, if already present, replaces the patient, If
	 * select is set or the patient is currently selected, an update will be
	 * performed.
	 * 
	 * @param patient
	 */
	public void add(Patient patient, boolean select) {
		if (contains(patient)) {
			replace(patient);
			// selecting patient
		} else {
			getItems().add(patient);
			generateTaskStatus(patient);
		}
		
		if (select) {
			selectPatient(patient);
		}
	}

	/**
	 * Adds a list of patients to the worklist, if one is already present in the
	 * worklist, the patient will be replaced
	 * 
	 * @param patients
	 */
	public void addAll(List<Patient> patients) {
		patients.forEach(p -> add(p));
	}

	/**
	 * Sets the selected Patient
	 * 
	 * @param patient
	 */
	public void selectPatient(Patient patient) {
		setSelectedPatient(patient);
		setSelectedTask(null);
	}

	/**
	 * Deselects the current patient.
	 */
	public boolean deselectPatient() {
		return deselectPatient(null);
	}

	/**
	 * Deselects a specific patient, if patient is null the current patient will be
	 * deselected.
	 * 
	 * @param patient
	 */
	public boolean deselectPatient(Patient patient) {
		if ((patient != null && patient.equals(getSelectedPatient())) || patient == null) {
			setSelectedPatient(null);
			setSelectedTask(null);
			return true;
		}
		return false;
	}

	/**
	 * Removes a patient from the worklist. If the patient is selected, the patient
	 * will be deseleced. Will return true if the patient was unselected.
	 * 
	 * @param toRemovePatient
	 */
	public boolean remove(Patient toRemovePatient) {
		for (Patient patient : items) {
			if (patient.equals(toRemovePatient)) {
				items.remove(patient);
				break;
			}
		}

		return deselectPatient(toRemovePatient);
	}

	/**
	 * Replaces a patient in the worklist
	 * 
	 * @param patient
	 * @return
	 */
	public boolean replace(Patient patient) {
		for (Patient pListItem : getItems()) {
			if (pListItem.equals(patient)) {
				int index = getItems().indexOf(pListItem);
				updateTaksActiveStatus(pListItem, patient);
				generateTaskStatus(patient);

				getItems().remove(pListItem);
				getItems().add(index, patient);

				// selecting patient
				if (patient.equals(getSelectedPatient()))
					selectPatient(patient);

				return true;
			}
		}
		return false;
	}

	/**
	 * Clears a worklist
	 */
	public boolean clear() {
		getItems().clear();
		return deselectPatient();
	}

	/**
	 * Sets a Task as selected task, also selects the patient
	 * 
	 * @param t
	 */
	public void setSelectedTaskAndPatient(Task t) {
		logger.debug("Adding task to worklist as selected task {} -> {}",
				getSelectedTask() == null ? "null" : getSelectedTask().getId(), t.getId());

		boolean changed = getSelectedTask() == null || getSelectedTask().getId() != t.getId();

		add(t.getPatient(), true);
		setSelectedTask(t);

		// only setting new task info if task has changed, this way data ca persists
		// over task realoads
		if (changed)
			setSelectedTaskInfo(new TaskInfo(t));
	}

	/**
	 * Deselects the task
	 * 
	 * @param task
	 * @return
	 */
	public boolean deselectTask() {
		return deselectTask(null);
	}

	/**
	 * Deselects the task
	 * 
	 * @param task
	 * @return
	 */
	public boolean deselectTask(Task task) {
		if ((task != null && task.equals(getSelectedTask())) || task == null) {
			setSelectedTask(null);
			return true;
		}
		return false;
	}

	public void updateTaksActiveStatus(Patient old, Patient newPat) {
		for (Task newTask : newPat.getTasks()) {
			for (Task oldTask : old.getTasks()) {
				if (newTask.equals(oldTask)) {
					newTask.setActive(oldTask.isActive());
				}
				break;
			}
		}
	}

	/**
	 * Returns the patient of the worklist
	 * 
	 * @return
	 */
	public Patient getFirstPatient() {
		if (isEmpty())
			return null;
		return items.get(0);
	}

	/**
	 * Returns the first Active task, otherwise null
	 * 
	 * @return
	 */
	public Task getFirstActiveTask() {
		if (isEmpty())
			return null;

		for (Patient patient : items) {
			if (!patient.getActiveTasks(isShowActiveTasksExplicit()).isEmpty())
				return patient.getActiveTasks(isShowActiveTasksExplicit()).get(0);
		}

		return null;
	}

	public void generateTaskStatus(Patient patient) {
		logger.debug("Generating new Taskstatus");
		if (patient.getTasks() != null)
			for (Task task : patient.getTasks()) {
				task.generateTaskStatus();
			}
	}

	public void sortWordklist() {
		sortWordklist(getWorklistSortOrder(), isSortAscending());
	}

	/**
	 * Sorts a list with patients either by task id or name of the patient
	 * 
	 * @param patiens
	 * @param order
	 */
	public void sortWordklist(WorklistSortOrder order, boolean asc) {
		switch (order) {
		case TASK_ID:
			// Sorting
			Collections.sort(items, new Comparator<Patient>() {
				@Override
				public int compare(Patient patientOne, Patient patientTwo) {
					Task lastTaskOne = patientOne.hasActiveTasks(showActiveTasksExplicit)
							? patientOne.getActiveTasks(showActiveTasksExplicit).get(0)
							: null;
					Task lastTaskTwo = patientTwo.hasActiveTasks(showActiveTasksExplicit)
							? patientTwo.getActiveTasks(showActiveTasksExplicit).get(0)
							: null;

					if (lastTaskOne == null && lastTaskTwo == null)
						return 0;
					else if (lastTaskOne == null)
						return asc ? -1 : 1;
					else if (lastTaskTwo == null)
						return asc ? 1 : -1;
					else {
						int res = lastTaskOne.getTaskID().compareTo(lastTaskTwo.getTaskID());
						return asc ? res : res * -1;
					}
				}
			});
			break;
		case PIZ:
			// Sorting
			Collections.sort(items, new Comparator<Patient>() {
				@Override
				public int compare(Patient patientOne, Patient patientTwo) {
					if (patientOne.getPiz() == null && patientTwo.getPiz() == null)
						return 0;
					else if (patientOne.getPiz() == null)
						return asc ? -1 : 1;
					else if (patientTwo.getPiz() == null)
						return asc ? 1 : -1;
					else {
						int res = patientOne.getPiz().compareTo(patientTwo.getPiz());
						return asc ? res : res * -1;
					}
				}
			});
			break;
		case NAME:
			Collections.sort(items, new Comparator<Patient>() {
				@Override
				public int compare(Patient patientOne, Patient patientTwo) {
					if (patientOne.getPerson().getLastName() == null && patientTwo.getPerson().getLastName() == null)
						return 0;
					else if (patientOne.getPerson().getLastName() == null)
						return asc ? -1 : 1;
					else if (patientTwo.getPerson().getLastName() == null)
						return asc ? 1 : -1;
					else {
						int res = patientOne.getPerson().getLastName().compareTo(patientTwo.getPerson().getLastName());
						return asc ? res : res * -1;
					}
				}
			});
			break;
		case PRIORITY:
			Collections.sort(items, new Comparator<Patient>() {
				@Override
				public int compare(Patient patientOne, Patient patientTwo) {
					Task highestPriorityOne = patientOne.hasActiveTasks(showActiveTasksExplicit)
							? TaskUtil.getTaskByHighestPriority(patientOne.getActiveTasks(showActiveTasksExplicit))
							: null;
					Task highestPriorityTwo = patientTwo.hasActiveTasks(showActiveTasksExplicit)
							? TaskUtil.getTaskByHighestPriority(patientTwo.getActiveTasks(showActiveTasksExplicit))
							: null;

					if (highestPriorityOne == null && highestPriorityTwo == null)
						return 0;
					else if (highestPriorityOne == null)
						return asc ? -1 : 1;
					else if (highestPriorityTwo == null)
						return asc ? 1 : -1;
					else {
						int res = highestPriorityOne.getTaskPriority().compareTo(highestPriorityTwo.getTaskPriority());
						return asc ? res : res * -1;
					}
				}
			});
			break;
		}
	}

	public boolean isEmpty() {
		return getItems().isEmpty();
	}

	public void updateWorklist() {
		updateWorklist(false);
	}

	public void updateWorklist(boolean updateSelectedPatient) {

		// executing worklistsearch
		List<Patient> update = getWorklistSearch().getPatients();
		for (Patient patient : update) {
			// Skipping if patient is active patient
			if (!patient.equals(selectedPatient)) {
				logger.trace("Updatin or adding: " + patient.toString());
				add(patient);
			} else if (updateSelectedPatient) {
				reloadSelectedPatientAndTask();
			} else
				logger.trace("Skippting " + selectedPatient.toString() + " (is selected patient)");
		}

		// fining patients which were not updated
		List<Long> manuallyUdatePizes = new ArrayList<Long>();
		boolean reloadCurrentTaskAndPatient = false;

		loop: for (Patient inList : getItems()) {
			for (Patient patient : update) {
				if (inList.equals(patient))
					continue loop;
			}

			// Skipping if patient is active patient
			if (!inList.equals(selectedPatient))
				manuallyUdatePizes.add(inList.getId());
			else
				reloadCurrentTaskAndPatient = true;
		}

		if (!manuallyUdatePizes.isEmpty()) {
			logger.trace("Loading patients not in worklist");
			// updating patients in worklist which were not found by generic
			// search

			List<Patient> histoMatchList = patientRepository.findAllByIds(manuallyUdatePizes, true, false, true);

			for (Patient patient : histoMatchList) {
				logger.trace("Updatin or adding: " + patient.toString());
				add(patient);
			}
		}

		if (reloadCurrentTaskAndPatient)
			reloadSelectedPatientAndTask();

	}

	/**
	 * Reloads the selected Task and patient if any is set
	 */
	public void reloadSelectedPatientAndTask() {
		if (selectedTask != null) {
			logger.debug("Reloading current Task and Patient");
			Optional<Task> oTask = taskRepository.findOptionalByIdAndInitialize(selectedTask.getId(), true, true, true,
					true, true);
			setSelectedTask(oTask.get());
			setSelectedPatient(oTask.get().getPatient());
			add(oTask.get().getPatient());
		} else if (selectedPatient != null) {
			logger.debug("Reloading Patient");
			Optional<Patient> oPatient = patientRepository.findOptionalById(selectedPatient.getId(), true);
			setSelectedPatient(oPatient.get());
			add(oPatient.get());
		} else {
			logger.debug("Error not reloading anything, should not happen!");
		}
	}

}
