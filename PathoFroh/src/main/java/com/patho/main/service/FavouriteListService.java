package com.patho.main.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.favourites.FavouriteListItem;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.FavouriteListItemRepository;
import com.patho.main.repository.FavouriteListRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.util.helper.StreamUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class FavouriteListService extends AbstractService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ResourceBundle resourceBundle;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListRepository favouriteListRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListItemRepository favouriteListItemRepository;

	public Task addTaskToList(long taskID, PredefinedFavouriteList predefinedFavouriteList) {
		return addTaskToList(taskID, predefinedFavouriteList.getId());
	}

	public Task addTaskToList(Task task, PredefinedFavouriteList predefinedFavouriteList) {
		return addTaskToList(task, predefinedFavouriteList.getId());
	}

	public Task addTaskToList(long taskID, long id) {
		return addTaskToList(taskID, null, new long[] { id });
	}

	public Task addTaskToList(Task task, long id) {
		return addTaskToList(task, null, new long[] { id });
	}

	public Task addTaskToList(long taskID, String commentary, Long... ids) {
		return addTaskToList(taskID, commentary, ArrayUtils.toPrimitive(ids));
	}

	public Task addTaskToList(long taskID, String commentary, PredefinedFavouriteList... predefinedFavouriteLists) {
		Task task = taskRepository.findOptionalById(taskID).get();
		return addTaskToList(task, commentary, predefinedFavouriteLists);
	}

	public Task addTaskToList(Task task, String commentary, PredefinedFavouriteList... predefinedFavouriteLists) {
		long[] listID = new long[predefinedFavouriteLists.length];
		for (int i = 0; i < predefinedFavouriteLists.length; i++) {
			listID[i] = predefinedFavouriteLists[i].getId();
		}

		return addTaskToList(task, commentary, listID);
	}

	public Task addTaskToList(long taskID, String commentary, long... ids) {
		Task task = taskRepository.findOptionalById(taskID).get();
		return addTaskToList(task, commentary, ids);
	}

	public Task addTaskToList(Task task, String commentary, long... ids) {
		for (int i = 0; i < ids.length; i++) {
			Optional<FavouriteList> oFavList = favouriteListRepository.findOptionalByIdAndInitialize(ids[i], true,
					false, false);
			task = addTaskToList(task, oFavList.get(), commentary);
		}

		return task;
	}

	public Task addTaskToList(Task task, FavouriteList favouriteList, String commentary) {

		// list should not contain the task
		if (favouriteList.getItems().stream().noneMatch(p -> p.getTask().getId() == task.getId())) {
			logger.debug("Adding task (" + task.getTaskID() + ") to favourite lists (" + favouriteList.getName()
					+ "), Kommentar: " + commentary);
			FavouriteListItem favItem = new FavouriteListItem(favouriteList, task);
			favItem.setCommentary(commentary != null ? commentary : "");
			// saving new fav item
			favouriteListItemRepository.save(favItem);
			favouriteList.getItems().add(favItem);
			// saving favlist
			favouriteListRepository.save(favouriteList);
		} else {
			logger.debug("List (" + favouriteList.getName() + ") already contains task (" + task.getTaskID() + ")");
		}

		// adding to task if task is not member of this list
		if (task.getFavouriteLists().stream().noneMatch(p -> p.getId() == favouriteList.getId())) {

			logger.debug("Adding favourite list(" + favouriteList.getName() + ") to task (" + task.getTaskID() + ")");

			task.getFavouriteLists().add(favouriteList);

			return taskRepository.save(task,
					resourceBundle.get("log.favouriteList.added", task.getTaskID(), favouriteList));

		} else
			logger.debug("Task (" + task.getTaskID() + ") alread contains list (" + favouriteList.getName() + ")");

		return task;
	}

	public void removeTaskFromList(long taskID, boolean addToDumpList, Long... ids) {
		removeTaskFromList(taskID, addToDumpList, ArrayUtils.toPrimitive(ids));
	}

	public void removeTaskFromList(long taskID, Long... ids) {
		removeTaskFromList(taskID, true, ids);
	}

	public Task removeTaskFromList(long taskID, boolean addToDumpList,
			PredefinedFavouriteList... predefinedFavouriteLists) {
		Task task = taskRepository.findOptionalById(taskID).get();
		return removeTaskFromList(task, addToDumpList, predefinedFavouriteLists);
	}

	public Task removeTaskFromList(long taskID, PredefinedFavouriteList... predefinedFavouriteLists) {
		return removeTaskFromList(taskID, true, predefinedFavouriteLists);
	}

	public Task removeTaskFromList(Task task, boolean addToDumpList,
			PredefinedFavouriteList... predefinedFavouriteLists) {

		long[] listID = new long[predefinedFavouriteLists.length];
		for (int i = 0; i < predefinedFavouriteLists.length; i++) {
			listID[i] = predefinedFavouriteLists[i].getId();
		}

		return removeTaskFromList(task, addToDumpList, listID);
	}

	public Task removeTaskFromList(Task task, PredefinedFavouriteList... predefinedFavouriteLists) {
		return removeTaskFromList(task, true, predefinedFavouriteLists);
	}

	public Task removeTaskFromList(long taskID, boolean addToDumpList, long... ids) {
		Task task = taskRepository.findOptionalById(taskID).get();
		return removeTaskFromList(task, addToDumpList, ids);
	}

	public Task removeTaskFromList(long taskID, long... ids) {
		return removeTaskFromList(taskID, true, ids);
	}

	public Task removeTaskFromList(Task task, boolean addToDumpList, long... ids) {
		for (int i = 0; i < ids.length; i++) {
			Optional<FavouriteList> oFavList = favouriteListRepository.findOptionalByIdAndInitialize(ids[i], true,
					false, false);
			task = removeTaskFromList(task, addToDumpList, oFavList.get());
		}

		return task;
	}

	public Task removeTaskFromList(Task task, long... ids) {
		return removeTaskFromList(task, true, ids);
	}

	public Task removeTaskFromList(Task task, boolean addToDumpList, FavouriteList favouriteList) {

		try {

			// task should be in fav list
			logger.debug(
					"Removing task (" + task.getTaskID() + ") from favourite lists (" + favouriteList.getName() + ")");

			// searching for item to remove
			FavouriteListItem itemToRemove = null;
			for (FavouriteListItem iter : favouriteList.getItems()) {
				if (iter.getTask() != null && iter.getTask().equals(task)) {
					itemToRemove = iter;
					break;
				}
			}

			if(itemToRemove == null)
				throw new IllegalStateException();
			
			favouriteList.getItems().remove(itemToRemove);
			// saving new fav item
			favouriteListRepository.save(favouriteList);

			favouriteListItemRepository.delete(itemToRemove,
					resourceBundle.get("log.favouriteList.removed", task.getTaskID(), favouriteList.toString()));
			
		} catch (IllegalStateException e) {
			// no item found
			logger.debug("Can not remove task (" + task.getTaskID() + ") from favourite list ("
					+ favouriteList.getName() + "), not in list");
		}

		try {
			logger.debug(
					"Removing favourite list(" + favouriteList.getName() + ") from task (" + task.getTaskID() + ")");

			FavouriteList listToRemove = task.getFavouriteLists().stream()
					.filter(p -> p.getId() == favouriteList.getId()).collect(StreamUtils.singletonCollector());

			task.getFavouriteLists().remove(listToRemove);

			// saving new fav item
			task = taskRepository.save(task);

		} catch (IllegalStateException e) {
			// no item found
			logger.debug("Can not remove favourite list(" + favouriteList.getName() + ") from task (" + task.getTaskID()
					+ "), not listed ");
		}

		if (addToDumpList && favouriteList.getDumpList() != null)
			task = addTaskToList(task, favouriteList.getDumpList().getId());

		return task;
		// TODO Delete FavouriteListItem?
	}

	public void removeTaskFromAllLists(Task task) {
		// removing from favouriteLists
		List<Long> list = task.getFavouriteLists().stream().map(p -> p.getId()).collect(Collectors.toList());
		removeTaskFromList(task.getId(), list.toArray(new Long[list.size()]));
	}

	/**
	 * Moves one Task from one List to an other list.
	 * 
	 * @param source
	 * @param target
	 * @param task
	 */
	public void moveTaskToList(long sourceID, long targetID, long taskID) {
		moveTaskToList(sourceID, targetID, taskID, null);
	}

	/**
	 * Moves one Task from one List to an other list.
	 * 
	 * @param source
	 * @param target
	 * @param task
	 */
	public void moveTaskToList(long sourceID, long targetID, long taskID, String commentary) {
		Optional<Task> oTask = taskRepository.findOptionalById(taskID);
		Optional<FavouriteList> oSource = favouriteListRepository.findOptionalByIdAndInitialize(sourceID, true, false,
				false);
		Optional<FavouriteList> oTarget = favouriteListRepository.findOptionalByIdAndInitialize(targetID, true, false,
				false);

		if (oTask.isPresent() && oSource.isPresent() && oTarget.isPresent()) {
			removeTaskFromList(oTask.get(), false, oSource.get());
			addTaskToList(oTask.get(), oTarget.get(), commentary);
		}
	}
}
