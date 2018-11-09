package com.patho.main.repository.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.DiagnosisPreset_;

@Service
@Transactional
public class DiagnosisPresetRepositoryImpl extends AbstractRepositoryCustom {

	public List<DiagnosisPreset> findAllIgnoreArchivedByOrderByIndexInListAsc(boolean ignoreArchived) {

		CriteriaQuery<DiagnosisPreset> criteria = getCriteriaBuilder().createQuery(DiagnosisPreset.class);
		Root<DiagnosisPreset> root = criteria.from(DiagnosisPreset.class);

		if (ignoreArchived)
			criteria.where(getCriteriaBuilder().equal(root.get(DiagnosisPreset_.archived), false));

		criteria.orderBy(getCriteriaBuilder().asc(root.get(DiagnosisPreset_.indexInList)));
		criteria.distinct(true);
		criteria.select(root);

		return getSession().createQuery(criteria).getResultList();
	}
}
