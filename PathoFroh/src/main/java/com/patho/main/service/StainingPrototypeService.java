package com.patho.main.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.Person;
import com.patho.main.model.Physician;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototypeDetails;
import com.patho.main.repository.StainingPrototypeDetailsRepository;
import com.patho.main.repository.StainingPrototypeRepository;

@Service
@Transactional
public class StainingPrototypeService extends AbstractService {

	@Autowired
	private StainingPrototypeRepository stainingPrototypeRepository;

	@Autowired
	private StainingPrototypeDetailsRepository stainingPrototypeDetailsRepository;

	public StainingPrototype addOrUpdate(StainingPrototype p, List<StainingPrototypeDetails> removeDetails) {

		p = stainingPrototypeRepository.save(p,
				resourceBundle.get(p.getId() == 0 ? "log.settings.staining.new" : "log.settings.staining.update", p));

		for (StainingPrototypeDetails stainingPrototypeDetails : removeDetails) {
			stainingPrototypeDetailsRepository.delete(stainingPrototypeDetails);
		}

		return p;
	}

	public StainingPrototype incrementContactPriorityCounter(StainingPrototype prototype) {
		prototype.
		if (p.isPresent()) {
			p.get().setPriorityCount(p.get().getPriorityCount() + 1);
			return physicianRepository.save(p.get(), resourceBundle.get("log.contact.priority.increment",
					p.get().getPerson().getFullName(), p.get().getPriorityCount()));
		}

		return null;
	}
}
