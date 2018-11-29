package com.patho.main.action.handler;

import java.util.List;
import java.util.Optional;

import org.primefaces.PrimeFaces;
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
public class WorklistViewHandler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Lazy
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalEditViewHandler globalEditViewHandler;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistHandler worklistHandler;

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
			if (worklistHandler.getCurrent().isPatientSelected())
				changeView(View.WORKLIST_PATIENT);
			else {
				// get first patient in worklist, show him
				Patient first = worklistHandler.getCurrent().getFirstPatient();
				if (first != null)
					selectPatientAndChangeView(first);
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
			if (worklistHandler.getCurrent().isTaskSelected()) {
				changeView(view);
				onSelectTaskAndPatient(worklistHandler.getCurrent().getSelectedTask());

			} else if (worklistHandler.getCurrent().isPatientSelected()) {
				// no task selected but patient
				// getting active tasks
				List<Task> tasks = worklistHandler.getCurrent().getSelectedPatient()
						.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit());

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
					if (!worklistHandler.getCurrent().isShowActiveTasksExplicit()
							&& !worklistHandler.getCurrent().getSelectedPatient().getTasks().isEmpty()) {
						changeView(view);
						onSelectTaskAndPatient(
								worklistHandler.getCurrent().getSelectedPatient().getTasks().iterator().next());
					} else {
						changeView(view, View.WORKLIST_NOTHING_SELECTED);
					}
				}

			} else {
				// nothing selected
				Task first = worklistHandler.getCurrent().getFirstActiveTask();

				// select the task
				if (first != null) {
					changeView(view);
					onSelectTaskAndPatient(first);
				} else {
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

	public void selectPatientAndChangeView(long patientID) {
		selectPatientAndChangeView(new Patient(patientID), true);
	}

	public void selectPatientAndChangeView(Patient patient) {
		selectPatientAndChangeView(patient, true);
	}

	public void selectPatientAndChangeView(Patient patient, boolean reload) {
		changeView(View.WORKLIST_PATIENT);
		onSelectPatient(patient, reload);
	}

	public void onSelectPatient(Patient patient) {
		onSelectPatient(patient, true);
	}

	public void onSelectPatient(Patient patient, boolean reloadPatient) {
		long test = System.currentTimeMillis();
		logger.info("start - > 0");

		if (patient == null) {
			logger.debug("Deselecting patient");
			worklistHandler.getCurrent().deselectPatient();
			changeView(View.WORKLIST_BLANK);
			return;
		}

		if (reloadPatient) {
			Optional<Patient> oPatient = patientRepository.findOptionalById(patient.getId(), true, true);
			if (oPatient.isPresent())
				patient = oPatient.get();
			else {
				logger.debug("Could not reload patient, abort!");
				worklistHandler.getCurrent().deselectPatient();
				changeView(View.WORKLIST_BLANK);
				return;
			}
		}

		// replacing patient, generating task status
		worklistHandler.getCurrent().add(patient, true);

		logger.debug("Select patient " + worklistHandler.getCurrent().getSelectedPatient().getPerson().getFullName());

		globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_MENU_MODEL);

		logger.info("end -> " + (System.currentTimeMillis() - test));
	}

	public void onSelectTaskAndPatient(long taskID) {
		onSelectTaskAndPatient(new Task(taskID), true);
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
				if (worklistHandler.getCurrent().isPatientSelected()) {
					onSelectPatient(worklistHandler.getSelectedPatient());
					MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.taskNoFound");
					PrimeFaces.current()
							.executeScript("clickButtonFromBean('#globalCommandsForm\\\\:refreshContentBtn')");
				}
				return;
			}
		}

		logger.debug("Selecting task " + task.getPatient().getPerson().getFullName() + " " + task.getTaskID());

		// replacing patient, generating task status
		worklistHandler.getCurrent().add(task, true);

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
		if (worklistHandler.getCurrent().deselectTask())
			goToNavigation(View.WORKLIST_PATIENT);
	}

	public void addWorklist(AbstractWorklistSearch worklistSearch, String name, boolean selected) {
		addWorklist(new Worklist(name, worklistSearch,
				userHandlerAction.getCurrentUser().getSettings().isWorklistHideNoneActiveTasks(),
				userHandlerAction.getCurrentUser().getSettings().getWorklistSortOrder(),
				userHandlerAction.getCurrentUser().getSettings().isWorklistAutoUpdate()), selected);
	}

	public void addWorklist(Worklist worklist, boolean selected) {
		// removing worklist if worklist with the same name is present
		try {
			Worklist cWorklist = worklistHandler.getWorklists().stream()
					.filter(p -> p.getName().equals(worklist.getName())).collect(StreamUtils.singletonCollector());

			worklistHandler.getWorklists().remove(cWorklist);
		} catch (IllegalStateException e) {
		}

		worklistHandler.getWorklists().add(worklist);

		if (selected) {
			worklistHandler.setCurrent(worklist);
			worklist.updateWorklist();
			goToNavigation();
		}

	}

	/**
	 * Removes a worklist an updates the view if the worklist is the current
	 * worklist
	 * 
	 * @param worklist
	 */
	public void removeWorklist(Worklist worklist) {
		worklistHandler.getWorklists().remove(worklist);
		if (worklistHandler.isSelectedWorklist(worklist)) {
			worklistHandler.setCurrent(new Worklist("", new AbstractWorklistSearch()));
			goToNavigation();
		}
	}

	public void clearWorklist(Worklist worklist) {
		// if a patient was selected and the worklist is the current worklist update
		// view
		if (worklist.clear() && worklistHandler.isSelectedWorklist(worklist))
			goToNavigation();
	}

	public Task addTaskToWorklist(long id) {
		return addTaskToWorklist(id, false);
	}

	public Task addTaskToWorklist(long id, boolean changeView) {
		Optional<Task> oTask = taskRepository.findOptionalByIdAndInitialize(id, false, true, true, true, true);
		if (oTask.isPresent())
			return addTaskToWorklist(oTask.get(), false);
		return null;
	}

	public Task addTaskToWorklist(Task task, boolean changeView) {

		changeView = changeView | userHandlerAction.getCurrentUser().getSettings().isAddTaskWithSingelClick();
		task.setActive(true);

		// selecting task if patient is in worklist, or if usersettings force it
		if (worklistHandler.getCurrent().contains(task.getPatient()) || changeView) {
			logger.debug("Showning task " + task.getTaskID());
			// reloading task and patient from database

			// // only selecting task if patient is already selected
			if (worklistHandler.getCurrent().isSelected(task.getPatient()) || changeView)
				onSelectTaskAndPatient(task, false);
			else
				worklistHandler.getCurrent().add(task.getPatient(), true);

			return task;
		}

		logger.debug("Adding task " + task.getTaskID() + " to worklist");

		worklistHandler.getCurrent().add(task.getPatient(), true);

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
	public void addPatientToWorkList(Patient patient, boolean changeView) {
		// change view to patient
		if (changeView)
			selectPatientAndChangeView(patient, false);
		else
			worklistHandler.getCurrent().add(patient, false);
	}

	/**
	 * Removes a patient from the worklist.
	 * 
	 * @param patient
	 */
	public void removePatientFromWorklist(Patient patient) {
		logger.debug("Removing Patient from Worklist: " + patient.getPerson().getFullName());

		// if patient is the selected patient the view has to be updated
		if (worklistHandler.getCurrent().remove(patient))
			goToNavigation();
	}

	public void replacePatientInWorklist(Patient patient) {
		replacePatientInWorklist(patient, true);
	}

	public void replacePatientInWorklist(Patient patient, boolean reload) {
		if (reload)
			patient = patientRepository.findOptionalById(patient.getId()).get();

		logger.debug("Replacing patient due to external changes!");
		worklistHandler.getCurrent().add(patient);
		if (worklistHandler.getCurrent().isSelected(patient))
			// generating task data, taskstatus is generated previously
			globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_MENU_MODEL);
	}

	public void replaceTaskInWorklist(Task task) {
		replaceTaskInWorklist(task, true);
	}

	public void replaceTaskInWorklist(Task task, boolean reload) {
		
		if (reload)
			task = taskRepository.findOptionalById(task.getId()).get();
		
		logger.debug("Replacing task due to external changes!");
		
		worklistHandler.getCurrent().add(task);
		
		if (worklistHandler.getCurrent().isSelected(task))
			// generating task data, taskstatus is generated previously
			globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_MENU_MODEL);

	}

	public void reloadCurrentTask() {
		replaceTaskInWorklist(worklistHandler.getSelectedTask(), true);
	}

	/**
	 * Selects the next task in List
	 */
	public void selectNextTask() {
		if (!worklistHandler.getCurrent().isEmpty()) {
			if (worklistHandler.getCurrent().getSelectedPatient() != null) {

				int indexOfTask = worklistHandler.getCurrent().getSelectedPatient()
						.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())
						.indexOf(worklistHandler.getCurrent().getSelectedTask());

				// next task is within the same patient
				if (indexOfTask - 1 >= 0) {
					onSelectTaskAndPatient(worklistHandler.getCurrent().getSelectedPatient()
							.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())
							.get(indexOfTask - 1));
					return;
				}

				int indexOfPatient = worklistHandler.getCurrent().getItems()
						.indexOf(worklistHandler.getCurrent().getSelectedPatient());

				if (indexOfPatient == -1)
					return;

				if (indexOfPatient - 1 >= 0) {
					Patient newPatient = worklistHandler.getCurrent().getItems().get(indexOfPatient - 1);

					if (newPatient.hasActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())) {
						onSelectTaskAndPatient(
								newPatient.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())
										.get(newPatient
												.getActiveTasks(
														worklistHandler.getCurrent().isShowActiveTasksExplicit())
												.size() - 1));
					} else {
						selectPatientAndChangeView(newPatient);
					}
				}
			} else {
				Patient newPatient = worklistHandler.getCurrent().getItems()
						.get(worklistHandler.getCurrent().getItems().size() - 1);

				if (newPatient.hasActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())) {
					onSelectTaskAndPatient(
							newPatient.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())
									.get(newPatient
											.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())
											.size() - 1));
				} else {
					selectPatientAndChangeView(newPatient);
				}
			}
		}
	}

	public void selectPreviouseTask() {
		if (!worklistHandler.getCurrent().isEmpty()) {
			if (worklistHandler.getCurrent().getSelectedPatient() != null) {

				int indexOfTask = worklistHandler.getCurrent().getSelectedPatient()
						.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())
						.indexOf(worklistHandler.getCurrent().getSelectedTask());

				// next task is within the same patient
				if (indexOfTask + 1 < worklistHandler.getCurrent().getSelectedPatient()
						.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit()).size()) {
					onSelectTaskAndPatient(worklistHandler.getCurrent().getSelectedPatient()
							.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())
							.get(indexOfTask + 1));
					return;
				}

				int indexOfPatient = worklistHandler.getCurrent().getItems()
						.indexOf(worklistHandler.getCurrent().getSelectedPatient());

				if (indexOfPatient == -1)
					return;

				if (indexOfPatient + 1 < worklistHandler.getCurrent().getItems().size()) {
					Patient newPatient = worklistHandler.getCurrent().getItems().get(indexOfPatient + 1);

					if (newPatient.hasActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())) {
						onSelectTaskAndPatient(newPatient
								.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit()).get(0));
					} else {
						selectPatientAndChangeView(newPatient);
					}
				}
			} else {
				Patient newPatient = worklistHandler.getCurrent().getItems().get(0);

				if (newPatient.hasActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit())) {
					onSelectTaskAndPatient(
							newPatient.getActiveTasks(worklistHandler.getCurrent().isShowActiveTasksExplicit()).get(0));
				} else {
					selectPatientAndChangeView(newPatient);
				}
			}
		}
	}

}
