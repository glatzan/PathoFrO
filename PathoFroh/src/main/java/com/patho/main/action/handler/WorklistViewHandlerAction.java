package com.patho.main.action.handler;

import java.util.List;
import java.util.Optional;

import javax.faces.context.FacesContext;

import org.primefaces.PrimeFaces;
import org.primefaces.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.handler.GlobalEditViewHandler.TaskInitilize;
import com.patho.main.common.View;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.util.helper.StreamUtils;
import com.patho.main.util.worklist.Worklist;
import com.patho.main.util.worklist.search.AbstractWorklistSearch;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Controller
@Scope("session")
@Getter
@Setter
public class WorklistViewHandlerAction {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Lazy
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalEditViewHandler globalEditViewHandler;

	@Autowired
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Lazy
	private DiagnosisViewHandlerAction diagnosisViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	/**
	 * Handler functions for the current worklist
	 */
	private WokrlistHandler currentWokrlistHandler = new WokrlistHandler();

	public void goToNavigation() {
		goToNavigation(globalEditViewHandler.getNavigationData().getCurrentView());
	}

	public void goToNavigation(View view) {

		logger.debug("Navigation goto: " + view);

		switch (view) {
		case WORKLIST_TASKS:
			changeView(View.WORKLIST_TASKS);
			globalEditViewHandler.generateViewData();
			break;
		case WORKLIST_PATIENT:
			// show patient if selected
			if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null)
				changeView(View.WORKLIST_PATIENT);
			else {
				// get first patient in worklist, show him
				Patient first = globalEditViewHandler.getWorklistData().getWorklist().getFirstPatient();
				if (first != null)
					goToSelectPatient(first);
				else
					// change view to blank
					changeView(View.WORKLIST_PATIENT, View.WORKLIST_NOTHING_SELECTED);
			}
			globalEditViewHandler.generateViewData();
			break;
		case WORKLIST_RECEIPTLOG:
		case WORKLIST_DIAGNOSIS:
		case WORKLIST_REPORT:
			// if task is select change view
			if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null
					&& globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask() != null) {
				changeView(view);
				onSelectTaskAndPatient(globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask());

			} else if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null) {
				// no task selected but patient
				// getting active tasks
				List<Task> tasks = globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient()
						.getActiveTasks(
								globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit());

				boolean found = false;

				// searching for the first not finalized task
				for (Task task : tasks) {
					if (!task.isFinalized()) {
						changeView(view);
						onSelectTaskAndPatient(task);
						found = true;
						break;
					}
				}

				// if all tasks are finalized selecting the first task
				if (!found) {
					// display first task, if all task should be shown and there is a task
					if (!globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit()
							&& !globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient().getTasks()
									.isEmpty()) {
						changeView(view);
						onSelectTaskAndPatient(globalEditViewHandler.getWorklistData().getWorklist()
								.getSelectedPatient().getTasks().iterator().next());
					} else {
						changeView(view, View.WORKLIST_NOTHING_SELECTED);
					}
				}

			} else {
				// nothing selected
				
				System.out.println("test");
				
				Task first = globalEditViewHandler.getWorklistData().getWorklist().getFirstActiveTask();

				// select the task
				if (first != null) {
					changeView(view);
					onSelectTaskAndPatient(first);
				} else {
					System.out.println("test test");
					// change view to blank
					changeView(view, View.WORKLIST_NOTHING_SELECTED);
				}
			}
			break;
		default:
			changeView(View.WORKLIST_BLANK);
		}

	}

	public void changeView(View view) {
		changeView(view, view);
	}

	public void changeView(View currentView, View displayView) {
		logger.debug("Changing view to " + currentView + " display view (" + displayView + ")");

		globalEditViewHandler.getNavigationData().setCurrentView(currentView);

		globalEditViewHandler.getNavigationData().setDisplayView(displayView);

		if (currentView.isLastSubviewAble()) {
			logger.debug("Setting last default view to " + currentView);
			globalEditViewHandler.getNavigationData().setLastDefaultView(currentView);
		}
	}

	public void goToSelectPatient(long patientID) {
		Patient p = new Patient();
		p.setId(patientID);
		goToSelectPatient(p, true);
	}

	public void goToSelectPatient(Patient patient) {
		goToSelectPatient(patient, true);
	}

	public void goToSelectPatient(Patient patient, boolean reload) {
		if (patient != null) {
			changeView(View.WORKLIST_PATIENT);
			onSelectPatient(patient, reload);
		}
	}

	public void onSelectPatient(Patient patient) {
		onSelectPatient(patient, true);
	}

	public void onSelectPatient(Patient patient, boolean reloadPatient) {
		long test = System.currentTimeMillis();
		logger.info("start - > 0");

		if (patient == null) {
			logger.debug("Deselecting patient");
			globalEditViewHandler.getWorklistData().getWorklist().addAsSelected((Patient)null);
			changeView(View.WORKLIST_BLANK);
			return;
		}

		if (reloadPatient) {
			Optional<Patient> oPatient = patientRepository.findOptionalById(patient.getId(), true, true);
			if (oPatient.isPresent())
				patient = oPatient.get();
			else {
				logger.debug("Could not reload patient, abort!");
				return;
			}
		}
		
		// replacing patient, generating task status
		globalEditViewHandler.getWorklistData().getWorklist().addAsSelected(patient);

		logger.debug("Select patient "
				+ globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient().getPerson().getFullName());

		globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_MENU_MODEL);

		logger.info("end -> " + (System.currentTimeMillis() - test));
	}

	public void onDeselectPatient() {
		onDeselectPatient(true);
	}

	public void onDeselectPatient(boolean updateView) {
		globalEditViewHandler.getWorklistData().getWorklist().addAsSelected((Patient)null);

		if (updateView)
			goToNavigation();
	}

	public void onSelectTaskAndPatient(long taskID) {
		Task t = new Task();
		t.setId(taskID);
		onSelectTaskAndPatient(t, true);

	}

	public void onSelectTaskAndPatient(Task task) {
		onSelectTaskAndPatient(task, true);
	}

	/**
	 * Selects a task and sets the patient of this task as selectedPatient
	 * 
	 * @param task
	 */
	public void onSelectTaskAndPatient(Task task, boolean reload) {
		long test = System.currentTimeMillis();
		logger.info("start - > 0");

		if (task == null) {
			logger.debug("Deselecting task");
			changeView(View.WORKLIST_BLANK);
			return;
		}

		if (reload) {

			logger.debug("Reloading task");

			Optional<Task> oTask = taskRepository.findOptionalByIdAndInitialize(task.getId(), false, true, true, true,
					true);

			if (oTask.isPresent()) {
				task = oTask.get();
			} else {
				// task might be delete from an other user
				if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null) {
					replacePatientInCurrentWorklist(
							globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient());

					// mainHandlerAction.sendGrowlMessagesAsResource("growl.error",
					// "growl.error.version");

					PrimeFaces.current()
							.executeScript("clickButtonFromBean('#globalCommandsForm\\\\:refreshContentBtn')");
				}
				return;
			}
		}

		logger.debug("Selecting task " + task.getPatient().getPerson().getFullName() + " " + task.getTaskID());

		// replacing patient, generating task status
		globalEditViewHandler.getWorklistData().getWorklist().addAsSelected(task);

		// task.setActive(true);

		// change if is subview (diagnosis, receipt log or report view)
		if (!globalEditViewHandler.getNavigationData().getCurrentView().isLastSubviewAble()) {
			logger.debug("Setting subview " + globalEditViewHandler.getNavigationData().getLastDefaultView());
			changeView(globalEditViewHandler.getNavigationData().getLastDefaultView());
		}

		// generating task data, taskstatus is generated previously
		globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_MENU_MODEL);

		logger.info("Request processed in -> " + (System.currentTimeMillis() - test));
	}

	/**
	 * Deselects a task an show the worklist patient view.
	 * 
	 * @param patient
	 * @return
	 */
	public void onDeselectTask() {
		globalEditViewHandler.getWorklistData().getWorklist().setSelectedTask(null);
		goToNavigation(View.WORKLIST_PATIENT);
	}

	public void addWorklist(AbstractWorklistSearch worklistSearch, String name, boolean selected) {
		addWorklist(new Worklist(name, worklistSearch,
				userHandlerAction.getCurrentUser().getSettings().isWorklistHideNoneActiveTasks(),
				userHandlerAction.getCurrentUser().getSettings().getWorklistSortOrder(),
				userHandlerAction.getCurrentUser().getSettings().isWorklistAutoUpdate()), selected);
	}

	public void addWorklist(Worklist worklist, boolean selected) {
		addWorklist(worklist, selected, false);
	}

	public void addWorklist(Worklist worklist, boolean selected, boolean changeView) {
		// removing worklist if worklist with the same name is present
		try {
			Worklist cWorklist = globalEditViewHandler.getWorklistData().getWorklists().stream()
					.filter(p -> p.getName().equals(worklist.getName())).collect(StreamUtils.singletonCollector());

			removeWorklist(cWorklist);
		} catch (IllegalStateException e) {
			// do nothing
		}

		globalEditViewHandler.getWorklistData().getWorklists().add(worklist);

		if (selected) {
			setWorklist(worklist);
			// deselecting patient
			onDeselectPatient(false);
			worklist.updateWorklist();
		}

		if (changeView)
			goToNavigation();
	}

	public void removeWorklist(Worklist worklist) {
		globalEditViewHandler.getWorklistData().getWorklists().remove(worklist);
		if (globalEditViewHandler.getWorklistData().getWorklist() == worklist)
			setWorklist(new Worklist("", new AbstractWorklistSearch()));
	}

	public void clearWorklist(Worklist worklist) {
		worklist.clear();
		if (globalEditViewHandler.getWorklistData().getWorklist() == worklist)
			onDeselectPatient();
	}

	public Task addTaskToCurrentWorklist(long id) {
		return addTaskToCurrentWorklist(id, false);
	}

	public Task addTaskToCurrentWorklist(long id, boolean changeView) {
		Optional<Task> oTask = taskRepository.findOptionalByIdAndInitialize(id, false, true, true, true, true);
		if (oTask.isPresent())
			return addTaskToCurrentWorklist(oTask.get(), false);
		return null;
	}

	public Task addTaskToCurrentWorklist(Task task, boolean changeView) {
		changeView = changeView | userHandlerAction.getCurrentUser().getSettings().isAddTaskWithSingelClick();
		// selecting task if patient is in worklist, or if usersettings force it
		if (globalEditViewHandler.getWorklistData().getWorklist().contains(task.getPatient()) || changeView) {
			logger.debug("Showning task " + task.getTaskID());
			// reloading task and patient from database

			// // only selecting task if patient is already selected
			if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null
					&& globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient().getId() == task
							.getPatient().getId()
					|| changeView)
				onSelectTaskAndPatient(task, false);

			return task;
		}

		logger.debug("Adding task " + task.getTaskID() + " to worklist");
		addPatientToCurrentWorkList(task.getPatient(), false, false, false);

		task.setActive(true);
		
		globalEditViewHandler.getWorklistData().getWorklist().addAsSelected(task);

		return task;
	}

	/**
	 * Adds a patient to the worklist. If already added it is check if the patient
	 * should be selected. If so the patient will be selected. The patient isn't
	 * added twice.
	 * 
	 * @param patient
	 * @param asSelectedPatient
	 */
	public void addPatientToCurrentWorkList(Patient patient, boolean changeToPatientView, boolean asSelectedPatient,
			boolean reloadPatient) {

		// change view to patient
		if (changeToPatientView)
			goToSelectPatient(patient, reloadPatient);
		// select patient, do not change view
		else if (asSelectedPatient)
			onSelectPatient(patient, reloadPatient);
		// only add patient do nothin else
		else {
			if (reloadPatient) {
				Optional<Patient> oPatient = patientRepository.findOptionalById(patient.getId(), true, true);
				if (oPatient.isPresent())
					patient = oPatient.get();
				else {
					logger.debug("Could not reload patient, abort!");
					return;
				}
			}

			globalEditViewHandler.getWorklistData().getWorklist().add(patient);
			globalEditViewHandler.getWorklistData().getWorklist().sortWordklist();
		}
	}

	/**
	 * Removes a patient from the worklist.
	 * 
	 * @param patient
	 */
	public void removePatientFromCurrentWorklist(Patient patient) {

		logger.debug("Removing Patient from Worklist: " + patient.getPerson().getFullName());

		globalEditViewHandler.getWorklistData().getWorklist().remove(patient);

		if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null
				&& globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient().equals(patient)) {
			onDeselectPatient(true);
		}
	}

	public void replaceSelectedTask() {
		if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask() != null)
			replaceTaskInCurrentWorklist(globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask(), true);
	}

	public void replaceTaskInCurrentWorklist(Task task) {
		replaceTaskInCurrentWorklist(task, true);
	}

	public void replaceTaskInCurrentWorklist(Task task, boolean reload) {
		if (reload)
			task = taskDAO.getTaskAndPatientInitialized(task.getId());

		// onVersionConflictPatient(task.getParent(), false);
		onSelectTaskAndPatient(task, false);
	}

	public void replacePatientInCurrentWorklist(Patient patient) {
		logger.debug("Replacing patient due to external changes!");
		if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null && globalEditViewHandler
				.getWorklistData().getWorklist().getSelectedPatient().getId() == patient.getId())
			globalEditViewHandler.getWorklistData().getWorklist().addAsSelected(patient);
		globalEditViewHandler.getWorklistData().getWorklist().add(patient);
	}

	// TODO move
	public static boolean isDialogContext() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.containsKey(Constants.DIALOG_FRAMEWORK.CONVERSATION_PARAM);
	}

	/**
	 * Selects the next task in List
	 */
	public void selectNextTask() {
		if (!globalEditViewHandler.getWorklistData().getWorklist().isEmpty()) {
			if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null) {

				int indexOfTask = globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient()
						.getActiveTasks(
								globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())
						.indexOf(globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask());

				// next task is within the same patient
				if (indexOfTask - 1 >= 0) {
					onSelectTaskAndPatient(globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient()
							.getActiveTasks(
									globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())
							.get(indexOfTask - 1));
					return;
				}

				int indexOfPatient = globalEditViewHandler.getWorklistData().getWorklist().getItems()
						.indexOf(globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient());

				if (indexOfPatient == -1)
					return;

				if (indexOfPatient - 1 >= 0) {
					Patient newPatient = globalEditViewHandler.getWorklistData().getWorklist().getItems()
							.get(indexOfPatient - 1);

					if (newPatient.hasActiveTasks(
							globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())) {
						onSelectTaskAndPatient(
								newPatient.getActiveTasks(globalEditViewHandler.getWorklistData().getWorklist()
										.isShowActiveTasksExplicit()).get(
												newPatient
														.getActiveTasks(globalEditViewHandler.getWorklistData()
																.getWorklist().isShowActiveTasksExplicit())
														.size() - 1));
					} else {
						goToSelectPatient(newPatient);
					}
				}
			} else {
				Patient newPatient = globalEditViewHandler.getWorklistData().getWorklist().getItems()
						.get(globalEditViewHandler.getWorklistData().getWorklist().getItems().size() - 1);

				if (newPatient.hasActiveTasks(
						globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())) {
					onSelectTaskAndPatient(
							newPatient
									.getActiveTasks(globalEditViewHandler.getWorklistData().getWorklist()
											.isShowActiveTasksExplicit())
									.get(newPatient.getActiveTasks(globalEditViewHandler.getWorklistData().getWorklist()
											.isShowActiveTasksExplicit()).size() - 1));
				} else {
					goToSelectPatient(newPatient);
				}
			}
		}
	}

	public void selectPreviouseTask() {
		if (!globalEditViewHandler.getWorklistData().getWorklist().isEmpty()) {
			if (globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient() != null) {

				int indexOfTask = globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient()
						.getActiveTasks(
								globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())
						.indexOf(globalEditViewHandler.getWorklistData().getWorklist().getSelectedTask());

				// next task is within the same patient
				if (indexOfTask + 1 < globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient()
						.getActiveTasks(
								globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())
						.size()) {
					onSelectTaskAndPatient(globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient()
							.getActiveTasks(
									globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())
							.get(indexOfTask + 1));
					return;
				}

				int indexOfPatient = globalEditViewHandler.getWorklistData().getWorklist().getItems()
						.indexOf(globalEditViewHandler.getWorklistData().getWorklist().getSelectedPatient());

				if (indexOfPatient == -1)
					return;

				if (indexOfPatient + 1 < globalEditViewHandler.getWorklistData().getWorklist().getItems().size()) {
					Patient newPatient = globalEditViewHandler.getWorklistData().getWorklist().getItems()
							.get(indexOfPatient + 1);

					if (newPatient.hasActiveTasks(
							globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())) {
						onSelectTaskAndPatient(newPatient.getActiveTasks(
								globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())
								.get(0));
					} else {
						goToSelectPatient(newPatient);
					}
				}
			} else {
				Patient newPatient = globalEditViewHandler.getWorklistData().getWorklist().getItems().get(0);

				if (newPatient.hasActiveTasks(
						globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())) {
					onSelectTaskAndPatient(newPatient
							.getActiveTasks(
									globalEditViewHandler.getWorklistData().getWorklist().isShowActiveTasksExplicit())
							.get(0));
				} else {
					goToSelectPatient(newPatient);
				}
			}
		}
	}

	public void setWorklist(Worklist worklist) {
		worklist.sortWordklist();
		globalEditViewHandler.getWorklistData().setWorklist(worklist);
	}

	public class WokrlistHandler {

		public void autoReloadCurrentWorklist() {
			if (globalEditViewHandler.getWorklistData().getWorklist().isAutoUpdate()) {
				logger.debug("Auto updating worklist");
				reloadCurrentWorklist(false);
			}
		}

		public void reloadCurrentWorklist() {
			reloadCurrentWorklist(true);
		}

		public void reloadCurrentWorklist(boolean updateAll) {
			globalEditViewHandler.getWorklistData().getWorklist().updateWorklist(updateAll);
		}

	}
}
