package com.patho.main.service;

import com.patho.main.model.MaterialPreset;
import com.patho.main.repository.jpa.MaterialPresetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MaterialPresetService extends AbstractService {

    @Autowired
    private MaterialPresetRepository materialPresetRepository;

    public MaterialPreset addOrUpdate(MaterialPreset m) {

        m = materialPresetRepository.save(m, resourceBundle
                .get(m.getId() == 0 ? "log.settings.material.new" : "log.settings.material.update", m.getName()));

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
            materialPresetRepository.delete(m, resourceBundle.get("log.settings.material.deleted", m.getName()));
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
