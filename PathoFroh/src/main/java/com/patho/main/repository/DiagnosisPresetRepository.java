package com.patho.main.repository;

import java.util.List;

import com.patho.main.model.DiagnosisPreset;
import com.patho.main.repository.service.DiagnosisPresetRepositoryCustom;

public interface DiagnosisPresetRepository
		extends BaseRepository<DiagnosisPreset, Long>, DiagnosisPresetRepositoryCustom {

	public List<DiagnosisPreset> findAllByOrderByIndexInListAsc();

}
