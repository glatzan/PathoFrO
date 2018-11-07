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
import com.patho.main.model.interfaces.ListOrder;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.StainingPrototypeDetailsRepository;
import com.patho.main.repository.StainingPrototypeRepository;

@Service
@Transactional
public class MaterialPresetService extends AbstractService {

	@Autowired
	private MaterialPresetRepository materialPresetRepository;

	public MaterialPreset addOrUpdate(MaterialPreset m) {

		m = materialPresetRepository.save(m, resourceBundle
				.get(m.getId() == 0 ? "llog.settings.material.new" : "log.settings.material.update", m.getName()));

		return m;
	}

	public MaterialPreset incrementPriorityCounter(MaterialPreset materialPreset) {
		Optional<MaterialPreset> p = materialPresetRepository.findById(materialPreset.getId());

		if (p.isPresent()) {
			p.get().setPriorityCount(p.get().getPriorityCount() + 1);
			return materialPresetRepository.save(p.get(), resourceBundle.get("log.settings.staining.priority.increment",
					p.get().getName(), p.get().getPriorityCount()));
		}

		return materialPreset;
	}

	@Transactional(propagation = Propagation.NEVER)
	public boolean deleteOrArchive(MaterialPreset m) {
		try {
			materialPresetRepository.delete(m, "log.settings.material.deleted");
			return true;
		} catch (Exception e) {
			archive(m, true);
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
	public MaterialPreset archive(MaterialPreset m, boolean archive) {
		m.setArchived(archive);
		return materialPresetRepository.save(m, resourceBundle
				.get(archive ? "log.settings.material.archived" : "log.material.staining.dearchived", m.getName()));
	}
}
