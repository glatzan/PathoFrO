package com.patho.main.service;

import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.DiagnosisRevisionRepository;
import com.patho.main.repository.TaskRepository;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
public class TaskService extends AbstractService {

	@Autowired
	private DiagnosisRevisionRepository diagnosisRevisionRepository;

	@Autowired
	private TaskRepository taskRepository;

	public Task copyHistologicalRecord(Diagnosis diagnosis, boolean overwrite)
			throws HistoDatabaseInconsistentVersionException {

		String text = overwrite ? diagnosis.getDiagnosisPrototype().getExtendedDiagnosisText()
				: diagnosis.getParent().getText() + "\r\n"
						+ diagnosis.getDiagnosisPrototype().getExtendedDiagnosisText();

		diagnosis.getParent().setText(text);

		return taskRepository.save(diagnosis.getTask(),
				resourceBundle.get("log.patient.task.diagnosisRevision.update", diagnosis.getDiagnosis()));
	}

	public void archiveTask(Task task) {
		// remove from all system lists
		favouriteListDAO.removeReattachedTaskFromList(task, PredefinedFavouriteList.values());

		// finalizing task
		task.setFinalizationDate(System.currentTimeMillis());
		task.setFinalized(true);

		if (task.getStainingCompletionDate() == 0)
			task.setStainingCompletionDate(System.currentTimeMillis());

		if (task.getDiagnosisCompletionDate() == 0)
			task.setDiagnosisCompletionDate(System.currentTimeMillis());

		if (task.getNotificationCompletionDate() == 0)
			task.setNotificationCompletionDate(System.currentTimeMillis());

		genericDAO.savePatientData(task, "log.patient.task.phase.archive", task);

	}

	public void restoreTask(Task task, String commentary) {
		// finalizing task
		task.setFinalizationDate(0);
		task.setFinalized(false);

		genericDAO.savePatientData(task, "log.patient.task.phase.restored", task, commentary);
	}

	/**
	 * Sets the task id if changed. NOTICE: Validation of task id has to be done
	 * manually
	 * 
	 * @param task
	 * @param newID
	 */
	public boolean changeTaskID(Task task, String newID) {
		// do nothing if the same
		if (newID != null && newID.equals(task.getTaskID()))
			return false;

		String oldId = task.getTaskID();
		task.setTaskID(newID);

		taskRepository.save(task, resourceBundle.get("log.task.changeID", oldId, newID));

		return true;
	}

	/**
	 * Cheks if task id is a valied id and not already set in database.
	 * 
	 * @param taskID
	 * @return
	 */
	public TaskIDValidity validateTaskID(String taskID) {
		if (HistoUtil.isNullOrEmpty(taskID) || taskID.length() != 6) {
			return TaskIDValidity.SHORT;
		} else if (!taskID.matches("[0-9]{6}")) {
			return TaskIDValidity.ONLY_NUMBERS;
		} else if (taskRepository.findOptionalByTaskId(taskID).isPresent()) {
			return TaskIDValidity.ALREADY_PRESENT;
		}

		return TaskIDValidity.VALIDE;
	}

	/**
	 * Renames task entities, if ignoreManuallyChangedEntities is fales, manual
	 * changes will not be renamed.
	 * 
	 * @param task
	 * @param ignoreManuallyChangedEntities
	 * @param save
	 * @return
	 */
	@Transactional
	public Task updateNamesOfTaskEnities(Task task, boolean ignoreManuallyChangedEntities, boolean save) {
		logger.debug("Updatining names, ignore manually altered: " + ignoreManuallyChangedEntities); 
		for (Sample sample : task.getSamples()) {
			sample.updateAllNames(true, ignoreManuallyChangedEntities);
		}

		if (save)
			return taskRepository.save(task, resourceBundle.get("log.task.nameUpdate", task));
		else
			return task;
	}

	public enum TaskIDValidity {
		VALIDE, SHORT, ONLY_NUMBERS, ALREADY_PRESENT;
	}
}
