package com.patho.main.repository;

import com.patho.main.model.DiagnosisPreset;
import com.patho.main.repository.service.DiagnosisPresetRepositoryCustom;

import java.util.List;

public interface DiagnosisPresetRepository
		extends BaseRepository<DiagnosisPreset, Long>, DiagnosisPresetRepositoryCustom {

	public List<DiagnosisPreset> findAllByOrderByIndexInListAsc();

}
