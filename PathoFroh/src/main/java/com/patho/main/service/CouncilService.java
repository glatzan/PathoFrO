package com.patho.main.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.CouncilState;
import com.patho.main.common.DateFormat;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.Council;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.CouncilRepository;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class CouncilService extends AbstractService {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private CouncilRepository councilRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FavouriteListService favouriteListService;

	/**
	 * Creates a new council
	 * 
	 * @param task
	 * @return
	 */
	public CouncilReturn createCouncil(Task task, boolean addToFavList) {

		Council council = new Council(task);
		council.setDateOfRequest(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
		council.setName(generateCouncilName(council));
		council.setCouncilState(CouncilState.EditState);
		council.setAttachedPdfs(new HashSet<PDFContainer>());
		council.setTask(task);

		task.getCouncils().add(council);

		// task does not need to be saved, because council is mapped by the council.task
		// id;
		council = councilRepository.save(council,
				resourceBundle.get("log.patient.task.council.create", task, council.getName()), task.getPatient());

		task = favouriteListService.addTaskToList(council.getTask(), "", PredefinedFavouriteList.Council,
				PredefinedFavouriteList.CouncilRequest);

		return new CouncilReturn(task, council);
	}

	/**
	 * Generates a council name
	 * 
	 * @param council
	 * @return
	 */
	public String generateCouncilName(Council council) {
		StringBuffer str = new StringBuffer();

		// name
		if (council.getCouncilPhysician() != null)
			str.append(council.getCouncilPhysician().getPerson().getFullName());
		else
			str.append(resourceBundle.get("dialog.council.data.newCouncil"));

		str.append(" ");

		LocalDateTime ldt = LocalDateTime.ofInstant(council.getDateOfRequest().toInstant(), ZoneId.systemDefault());

		// adding date
		str.append(ldt.format(DateTimeFormatter.ofPattern(DateFormat.GERMAN_DATE.getDateFormat())));

		return str.toString();
	}

	/**
	 * Ends council request phase
	 * 
	 * @param council
	 * @return
	 */
	public CouncilReturn endCouncilRequest(Council council, PredefinedFavouriteList nextStepList) {
		council.setCouncilRequestCompleted(true);

		council = councilRepository.save(council,
				resourceBundle.get("log.patient.task.council.phase.request.end", council.getTask(), council.getName()),
				council.getTask().getPatient());

		Task task = favouriteListService.removeTaskFromList(council.getTask(), PredefinedFavouriteList.CouncilRequest);
//		task = favouriteListService.addTaskToList(task, nextStepList);

		return new CouncilReturn(task, council);
	}

	@Getter
	@Setter
	public static class CouncilReturn {
		protected Task task;
		protected Council council;

		public CouncilReturn(Task task, Council council) {
			this.task = task;
			this.council = council;
		}
	}
}
