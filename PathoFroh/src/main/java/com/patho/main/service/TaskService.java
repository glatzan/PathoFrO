package com.patho.main.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.buffer.BlockingBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.Council;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.DiagnosisRevisionRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.ui.task.TaskArchivationStatus;
import com.patho.main.ui.task.TaskStatus;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.helper.TimeUtil;

import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
@ConfigurationProperties(prefix = "patho.taskservice")
public class TaskService extends AbstractService {

	@Autowired
	private DiagnosisRevisionRepository diagnosisRevisionRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Getter
	@Setter
	private List<PredefinedFavouriteList> listsBlockingTaskArchivation;

	/**
	 * Creatse a task
	 * 
	 * @param patient
	 * @param taskID
	 * @param save
	 * @return
	 */
	public Task createTask(Patient patient, String taskID, boolean save) {
		Task task = new Task();
		task.setParent(patient);
		task.setCaseHistory("");
		task.setWard("");
		task.setInsurance(patient.getInsurance());

		if (HistoUtil.isNotNullOrEmpty(taskID)) {
			if (isTaskIDAvailable(taskID))
				task.setTaskID(taskID);
			else
				throw new IllegalArgumentException("Task ID taken");
		} else
			task.setTaskID(getNextTaskID());

		task.setCouncils(new LinkedHashSet<Council>());
		task.setFavouriteLists(new ArrayList<FavouriteList>());
		task.setDiagnosisRevisions(new LinkedHashSet<DiagnosisRevision>());
		task.setContacts(new LinkedHashSet<AssociatedContact>());
		task.setAttachedPdfs(new LinkedHashSet<PDFContainer>());

		if (patient.getTasks() == null)
			patient.setTasks(new LinkedHashSet<Task>());

		if (save)
			return task = taskRepository.save(task, resourceBundle.get("log.patient.task.new", task), patient);
		return task;

	}

	public Task copyHistologicalRecord(Diagnosis diagnosis, boolean overwrite)
			throws HistoDatabaseInconsistentVersionException {

		String text = overwrite ? diagnosis.getDiagnosisPrototype().getExtendedDiagnosisText()
				: diagnosis.getParent().getText() + "\r\n"
						+ diagnosis.getDiagnosisPrototype().getExtendedDiagnosisText();

		diagnosis.getParent().setText(text);

		return taskRepository.save(diagnosis.getTask(),
				resourceBundle.get("log.patient.task.diagnosisRevision.update", diagnosis.getDiagnosis()));
	}

	public boolean taskArchiveAble(Task task) {

		return true;
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

	public Task restoreTask(Task task, String commentary) {
		// finalizing task
		task.setFinalizationDate(0);
		task.setFinalized(false);

		return taskRepository.save(task,
				resourceBundle.get("log.patient.task.phase.restored", task, commentary == null ? "" : commentary));
	}

	public TaskArchivationStatus getTaskArchivationStatus(Task task) {
		TaskArchivationStatus result = new TaskArchivationStatus(task);

		result.setBlockingLists(findListsBlockingTaskArchivation(task));

		result.setStainingNeeded(!TaskStatus.checkIfStainingCompleted(task));
		result.setDiangosisNeeded(!TaskStatus.checkIfDiagnosisCompleted(task));
		result.setNotificationNeeded(!TaskStatus.checkIfNotificationisCompleted(task));

		result.setArchiveAble(!result.isStainingNeeded() && !result.isNotificationNeeded()
				&& !result.isDiangosisNeeded() && !result.isCouncilNotCompleted()
				&& (result.getBlockingLists() == null || result.getBlockingLists().size() == 0));

		return result;
	}

	public List<FavouriteList> findListsBlockingTaskArchivation(Task task) {
		return findListsBlockingTaskArchivation(task, getListsBlockingTaskArchivation());
	}

	public List<FavouriteList> findListsBlockingTaskArchivation(Task task,
			List<PredefinedFavouriteList> blockingLists) {
		List<FavouriteList> currentlyBlockingLists = new ArrayList<FavouriteList>();

		for (PredefinedFavouriteList predefinedFavouriteList : blockingLists) {
			for (FavouriteList list : task.getFavouriteLists()) {
				if (list.getId() == predefinedFavouriteList.getId()) {
					currentlyBlockingLists.add(list);
					break;
				}
			}
		}

		return currentlyBlockingLists;
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

	/**
	 * Returns true if the task id is not used.
	 * 
	 * @param id
	 * @return
	 */
	public boolean isTaskIDAvailable(String id) {
		return !taskRepository.findOptionalByTaskId(id).isPresent();
	}

	/**
	 * Returns the next id for a task, if the year changes a new task id will be
	 * generated
	 * 
	 * @return
	 */
	public String getNextTaskID() {
		// generating new task id
		Optional<Task> task = taskRepository.findOptinalByLastID(Calendar.getInstance(), false, false, false, false,
				false);

		// task is within the current year
		if (task.isPresent()) {
			// getting counter
			String count = task.get().getTaskID().substring(2, 6);
			// increment counter
			int counterAsInt = Integer.valueOf(count) + 1;
			return Integer.toString(TimeUtil.getCurrentYear() - 2000) + HistoUtil.fitString(counterAsInt, 4, '0');
		} else {
			// first task ever, or first task of year , year + 0001
			return Integer.toString(TimeUtil.getCurrentYear() - 2000) + HistoUtil.fitString(1, 4, '0');
		}
	}

	public enum TaskIDValidity {
		VALIDE, SHORT, ONLY_NUMBERS, ALREADY_PRESENT;
	}
}
