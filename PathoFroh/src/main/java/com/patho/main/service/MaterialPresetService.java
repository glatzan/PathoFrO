package com.patho.main.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.action.handler.MessageHandler;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.Physician;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototypeDetails;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.StainingPrototypeDetailsRepository;
import com.patho.main.repository.StainingPrototypeRepository;

@Service
@Transactional
public class MaterialPresetService extends AbstractService {

	@Autowired
	private MaterialPresetRepository materialPresetRepository;

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

	public StainingPrototype incrementPriorityCounter(MaterialPreset materialPreset) {
		Optional<MaterialPreset> p = materialPresetRepository.findById(materialPreset.getId());

		if (p.isPresent()) {
			p.get().setPriorityCount(p.get().getPriorityCount() + 1);
			return stainingPrototypeRepository.save(p.get(), resourceBundle
					.get("log.settings.staining.priority.increment", p.get().getName(), p.get().getPriorityCount()));
		}

		return prototype;
	}

	/**
	 * Tries to delete the stainingprototype, if not possible the prototype will be
	 * deleted
	 * 
	 * @param p
	 */
	@Transactional(propagation = Propagation.NEVER)
	public boolean deleteOrArchive(StainingPrototype p) {
		try {
			stainingPrototypeRepository.delete(p, "log.settings.staining.deleted");
			return true;
		} catch (Exception e) {
			archive(p, true);
			return false;
		}
	}

	/**
	 * Archived or dearchives a prototype
	 * 
	 * @param p
	 * @param archive
	 * @return
	 */
	public StainingPrototype archive(StainingPrototype p, boolean archive) {
		p.setArchived(archive);
		return stainingPrototypeRepository.save(p, resourceBundle
				.get(archive ? "log.settings.staining.archived" : "log.settings.staining.dearchived", p.getName()));
	}
}
