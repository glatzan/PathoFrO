package com.patho.main.service;

import com.patho.main.model.interfaces.ListOrder;
import com.patho.main.model.system.DiagnosisPreset;
import com.patho.main.repository.jpa.DiagnosisPresetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DiagnosisPresetService extends AbstractService {

    @Autowired
    private DiagnosisPresetRepository diagnosisPresetRepository;

    public DiagnosisPreset addOrUpdate(DiagnosisPreset d) {

        if (d.getId() == 0) {
            long count = diagnosisPresetRepository.count();
            d.setIndexInList((int) count);
        }

        d = diagnosisPresetRepository.save(d, resourceBundle.get(
                d.getId() == 0 ? "log.settings.diagnosis.new" : "log.settings.diagnosis.update", d.getDiagnosis()));

        return d;
    }

    /**
     * Tries to delete the reportIntent, if not possible the prototype will be archived
     *
     */
    @Transactional(propagation = Propagation.NEVER)
    public boolean deleteOrArchive(DiagnosisPreset d) {
        try {
            diagnosisPresetRepository.delete(d, resourceBundle.get("log.settings.diagnosis.deleted", d.getDiagnosis()));
            return true;
        } catch (Exception e) {
            archive(d, true);
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
    public DiagnosisPreset archive(DiagnosisPreset d, boolean archive) {
        d.setArchived(archive);
        return diagnosisPresetRepository.save(d, resourceBundle.get(
                archive ? "log.settings.diagnosis.archived" : "log.settings.diagnosis.dearchived", d.getDiagnosis()));
    }

    /**
     * Saves all elements on reoder
     *
     * @param presets
     * @return
     */
    public List<DiagnosisPreset> reoderList(List<DiagnosisPreset> presets) {
        ListOrder.reOrderList(presets);
        return diagnosisPresetRepository.saveAll(presets, resourceBundle.get("log.settings.diagnosis.list.reoder"));
    }
}
