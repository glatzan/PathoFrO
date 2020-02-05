package com.patho.main.repository.jpa.custom.impl;

import com.patho.main.model.preset.DiagnosisPreset;
import com.patho.main.model.preset.DiagnosisPreset_;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

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
