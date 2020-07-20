package com.patho.main.repository.jpa;

import com.patho.main.model.preset.DiagnosisPreset;
import com.patho.main.repository.jpa.custom.DiagnosisPresetRepositoryCustom;

import java.util.List;

public interface DiagnosisPresetRepository
        extends BaseRepository<DiagnosisPreset, Long>, DiagnosisPresetRepositoryCustom {

    public List<DiagnosisPreset> findAllByOrderByIndexInListAsc();

}
