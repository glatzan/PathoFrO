package com.patho.main.repository.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.MaterialPreset;
import com.patho.main.model.MaterialPreset_;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype_;
import com.patho.main.model.StainingPrototype.StainingType;
import com.patho.main.repository.service.MaterialPresetRepositoryCustom;

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

	public List<StainingPrototype> findAllIgnoreArchivedOrderByPriorityCountDesc(StainingType type,
			boolean loadStainings, boolean irgnoreArchived) {
		CriteriaBuilder qb = getCriteriaBuilder();
		CriteriaQuery<MaterialPreset> criteria = qb.createQuery(MaterialPreset.class);
		Root<MaterialPreset> root = criteria.from(MaterialPreset.class);

		criteria.select(root);

		criteria.where(getCriteriaBuilder().like(getCriteriaBuilder().lower(root.get(MaterialPreset_.name)),
				name.toLowerCase()));

		if (loadStainings)
			root.fetch(MaterialPreset_.stainingPrototypes, JoinType.LEFT);

		criteria.orderBy(getCriteriaBuilder().desc(root.get(MaterialPreset.priorityCount)));
		
		criteria.distinct(true);

		return getSession().createQuery(criteria).getResultList();

	}
}
