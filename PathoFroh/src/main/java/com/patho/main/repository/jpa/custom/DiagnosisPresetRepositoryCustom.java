package com.patho.main.repository.jpa.custom;

import com.patho.main.model.system.DiagnosisPreset;

import java.util.List;

public interface DiagnosisPresetRepositoryCustom {
    public List<DiagnosisPreset> findAllIgnoreArchivedByOrderByIndexInListAsc(boolean ignoreArchived);
}
