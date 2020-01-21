package com.patho.main.repository.jpa.custom.impl;

import com.patho.main.model.MaterialPreset;
import com.patho.main.model.MaterialPreset_;
import com.patho.main.repository.jpa.custom.MaterialPresetRepositoryCustom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
@Transactional
public class MaterialPresetRepositoryImpl extends AbstractRepositoryCustom implements MaterialPresetRepositoryCustom {

    public List<MaterialPreset> findAll(boolean loadStainings) {
        CriteriaBuilder qb = getCriteriaBuilder();
        CriteriaQuery<MaterialPreset> criteria = qb.createQuery(MaterialPreset.class);
        Root<MaterialPreset> root = criteria.from(MaterialPreset.class);

        criteria.select(root);

        if (loadStainings)
            root.fetch(MaterialPreset_.stainingPrototypes, JoinType.LEFT);

        criteria.distinct(true);

        return getSession().createQuery(criteria).getResultList();
    }

    public List<MaterialPreset> findAllByName(String name, boolean loadStainings) {
        CriteriaBuilder qb = getCriteriaBuilder();
        CriteriaQuery<MaterialPreset> criteria = qb.createQuery(MaterialPreset.class);
        Root<MaterialPreset> root = criteria.from(MaterialPreset.class);

        criteria.select(root);

        criteria.where(getCriteriaBuilder().like(getCriteriaBuilder().lower(root.get(MaterialPreset_.name)),
                name.toLowerCase()));

        if (loadStainings)
            root.fetch(MaterialPreset_.stainingPrototypes, JoinType.LEFT);

        criteria.distinct(true);

        return getSession().createQuery(criteria).getResultList();
    }

    public List<MaterialPreset> findAllIgnoreArchivedOrderByPriorityCountDesc(boolean loadStainings,
                                                                              boolean irgnoreArchived) {
        CriteriaBuilder qb = getCriteriaBuilder();
        CriteriaQuery<MaterialPreset> criteria = qb.createQuery(MaterialPreset.class);
        Root<MaterialPreset> root = criteria.from(MaterialPreset.class);

        criteria.select(root);

        if (loadStainings)
            root.fetch(MaterialPreset_.stainingPrototypes, JoinType.LEFT);

        if (irgnoreArchived)
            criteria.where(getCriteriaBuilder().equal(root.get(MaterialPreset_.archived), false));

        criteria.orderBy(getCriteriaBuilder().desc(root.get(MaterialPreset_.priorityCount)));

        criteria.distinct(true);

        return getSession().createQuery(criteria).getResultList();

    }
}
