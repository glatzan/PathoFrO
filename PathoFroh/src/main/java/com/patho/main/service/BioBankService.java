package com.patho.main.service;

import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.miscellaneous.BioBank;
import com.patho.main.repository.BioBankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;

@Service
@Transactional
public class BioBankService extends AbstractService {

	@Autowired
	private BioBankRepository bankRepository;

	public BioBank createBioBank(Task task) {
		BioBank bioBank = new BioBank();
		bioBank.setTask(task);
		bioBank.setAttachedPdfs(new LinkedHashSet<PDFContainer>());

		bioBank = bankRepository.save(bioBank, resourceBundle.get("log.patient.bioBank.created", task),
				task.getPatient());
		
		return bioBank;
	}
}
