package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.DiagnosisPreset;

public interface DiagnosisPresetRepository extends BaseRepository<DiagnosisPreset, Long> {
	
	public List<DiagnosisPreset> findAllByOrderByIndexInListAsc();
	
}
