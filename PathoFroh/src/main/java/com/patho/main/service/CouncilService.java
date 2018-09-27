package com.patho.main.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.CouncilState;
import com.patho.main.common.DateFormat;
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

	public Council createCouncil(Task task) {

		Council council = new Council(task);
		council.setDateOfRequest(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
		council.setName(generateCouncilName(council));
		council.setCouncilState(CouncilState.EditState);
		council.setAttachedPdfs(new HashSet<PDFContainer>());

		// task does not need to be saved, because council is mapped by the council.task
		// id;
		council = councilRepository.save(council,
				resourceBundle.get("log.patient.task.council.create", task, council.getName()), task.getPatient());

		return council;
	}

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
}
