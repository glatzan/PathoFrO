package com.patho.main.repository.service;

import java.util.List;

import com.patho.main.model.DiagnosisPreset;

public interface DiagnosisPresetRepositoryCustom {
	public List<DiagnosisPreset> findAllIgnoreArchivedByOrderByIndexInListAsc(boolean ignoreArchived);
}
