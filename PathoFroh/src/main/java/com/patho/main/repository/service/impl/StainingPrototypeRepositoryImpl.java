package com.patho.main.repository.service.impl;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype_;
import com.patho.main.repository.service.StainingPrototypeRepositoryCustom;

@Service
@Transactional
public class StainingPrototypeRepositoryImpl extends AbstractRepositoryCustom implements StainingPrototypeRepositoryCustom {

	public Optional<StainingPrototype> findOptionalByIdAndInitilize(Long id, boolean initializeBatch) {
		CriteriaQuery<StainingPrototype> criteria = getCriteriaBuilder().createQuery(StainingPrototype.class);
		Root<StainingPrototype> root = criteria.from(StainingPrototype.class);

		criteria.select(root);

		if (initializeBatch)
			root.fetch(StainingPrototype_.batchDetails, JoinType.LEFT);
		
		criteria.where(getCriteriaBuilder().equal(root.get(StainingPrototype_.id), id));

		criteria.distinct(true);

		List<StainingPrototype> stainingPrototypes = getSession().createQuery(criteria).getResultList();

		return Optional.ofNullable(stainingPrototypes.size() > 0 ? stainingPrototypes.get(0) : null);
	}
}
