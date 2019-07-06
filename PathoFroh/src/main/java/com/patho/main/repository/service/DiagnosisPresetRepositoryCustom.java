package com.patho.main.repository.service;

import com.patho.main.model.DiagnosisPreset;

import java.util.List;

public interface DiagnosisPresetRepositoryCustom {
	public List<DiagnosisPreset> findAllIgnoreArchivedByOrderByIndexInListAsc(boolean ignoreArchived);
}
