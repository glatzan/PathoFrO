package com.patho.main.service;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototypeDetails;
import com.patho.main.repository.jpa.StainingPrototypeDetailsRepository;
import com.patho.main.repository.jpa.StainingPrototypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StainingPrototypeService extends AbstractService {

    @Autowired
    private StainingPrototypeRepository stainingPrototypeRepository;

    @Autowired
    private StainingPrototypeDetailsRepository stainingPrototypeDetailsRepository;

    public StainingPrototype addOrUpdate(StainingPrototype p, List<StainingPrototypeDetails> removeDetails) {

        p = stainingPrototypeRepository.save(p,
                resourceBundle.get(p.getId() == 0 ? "log.settings.staining.new" : "log.slide.edit.textUpdate", p));

        for (StainingPrototypeDetails stainingPrototypeDetails : removeDetails) {
            stainingPrototypeDetailsRepository.delete(stainingPrototypeDetails);
        }

        return p;
    }

    public StainingPrototype incrementPriorityCounter(StainingPrototype prototype) {
        Optional<StainingPrototype> p = stainingPrototypeRepository.findById(prototype.getId());

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
            stainingPrototypeRepository.delete(p, resourceBundle.get("log.settings.staining.deleted", p.getName()));
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
