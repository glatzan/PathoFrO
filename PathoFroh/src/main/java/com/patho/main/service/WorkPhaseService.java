package com.patho.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.DiagnosisRevision.NotificationStatus;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.TaskRepository;
import com.patho.main.ui.task.TaskStatus;
import com.patho.main.util.helper.HistoUtil;

@Service
@Transactional
public class WorkPhaseService extends AbstractService {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private FavouriteListService favouriteListService;

	@Autowired
	private SlideService slideService;

	@Autowired
	private DiagnosisService diagnosisService;

	/**
	 * Updates the status of the stating phase, if the phase has ended, true will be
	 * returned.
	 * 
	 * @param task
	 */
	@Transactional
	public boolean updateStainigPhase(Task task) {
		// removing from staining list and showing the dialog for ending
		// staining phase
		if (TaskStatus.checkIfStainingCompleted(task)) {
			logger.trace("Staining phase of task (" + task.getTaskID()
					+ ") completed removing from staing list, adding to diagnosisList");
			// do nothin is done manual via dialog
			return true;
		} else {
			// reentering the staining phase, adding task to staining or
			// restaining list
			return false;
		}
	}

	/**
	 * Start the staining phase, adds the task to the staining or restaining phase,
	 * set staining completion date to 0
	 * 
	 * Error-Handling via global Error-Handler
	 * 
	 * @param task
	 */
	@Transactional
	public Task startStainingPhase(Task task) {

		if (task.getStainingCompletionDate() != 0) {
			task.setStainingCompletionDate(0);
			task = taskRepository.save(task, resourceBundle.get("log.phase.staining.enter"));
		}

		if (!task.isListedInFavouriteList(PredefinedFavouriteList.StainingList,
				PredefinedFavouriteList.ReStainingList)) {
			if (TaskStatus.checkIfReStainingFlag(task)) {
				task = favouriteListService.removeTaskFromList(task, PredefinedFavouriteList.StainingList);
				task = favouriteListService.addTaskToList(task, PredefinedFavouriteList.ReStainingList);
			} else
				task = favouriteListService.addTaskToList(task, PredefinedFavouriteList.StainingList);
		}

		return task;
	}

	/**
	 * Ends the staining phase, removes the task from the staining lists, sets
	 * staining completion date to current time
	 * 
	 * Error-Handling via global Error-Handler
	 * 
	 * @param task
	 */
	@Transactional
	public Task endStainingPhase(Task task, boolean removeFromList) {
		slideService.completedStaining(task, true);
		task.setStainingCompletionDate(System.currentTimeMillis());

		task = taskRepository.save(task, resourceBundle.get("log.phase.staining.end"));

		if (removeFromList)
			task = favouriteListService.removeTaskFromList(task, PredefinedFavouriteList.StainingList,
					PredefinedFavouriteList.ReStainingList);

		return task;
	}

	/**
	 * Starts the diagnosis phase, sets the time of diagnosis completion to zero and
	 * adds the task to the diagnosis list.
	 * 
	 * @param task
	 */
	@Transactional
	public Task startDiagnosisPhase(Task task) {
		task.setDiagnosisCompletionDate(0);

		task = taskRepository.save(task, resourceBundle.get("log.phase.diagnosis.enter"));

		if (!task.isListedInFavouriteList(PredefinedFavouriteList.DiagnosisList,
				PredefinedFavouriteList.ReDiagnosisList)) {
			task = favouriteListService.addTaskToList(task.getId(), PredefinedFavouriteList.DiagnosisList);
		}

		return task;
	}

	/**
	 * Ends the diagnosis phase, removes from diagnosis list and sets the diagnosis
	 * time of completion.
	 * 
	 * @param task
	 * @param updateSignatureDate
	 */
	public Task endDiagnosisPhase(Task task, boolean removeFromList, NotificationStatus status) {

		boolean change = false;
		// setting diagnosis compleation date if not set jet
		for (int i = 0; i < task.getDiagnosisRevisions().size(); i++) {
			DiagnosisRevision rev = HistoUtil.getNElement(task.getDiagnosisRevisions(), i);
			if (rev.getCompletionDate() == 0) {
				task = diagnosisService.approveDiangosis(task, rev, status);
				change = true;
			}
		}

		if (change || task.getDiagnosisCompletionDate() == 0) {
			task.setDiagnosisCompletionDate(System.currentTimeMillis());
			task = taskRepository.save(task, resourceBundle.get("log.phase.diagnosis.end"));
		}
		
		if (removeFromList)
			task = favouriteListService.removeTaskFromList(task, PredefinedFavouriteList.DiagnosisList,
					PredefinedFavouriteList.ReDiagnosisList);
		return task;
	}
}
