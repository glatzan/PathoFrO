package com.patho.main.repository.service.impl;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.BioBank;
import com.patho.main.model.BioBank_;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.service.BioBankRepositoryCustom;

@Service
@Transactional
public class BioBankRepositoryImpl extends AbstractRepositoryCustom implements BioBankRepositoryCustom {

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id) {
		return findOptionalByIdAndInitialize(id, false, true);
	}

	public Optional<BioBank> findOptionalByIdAndInitialize(Long id, boolean loadTask, boolean loadPDFs) {
		CriteriaBuilder qb = getCriteriaBuilder();
		CriteriaQuery<BioBank> criteria = qb.createQuery(BioBank.class);
		Root<BioBank> root = criteria.from(BioBank.class);

		criteria.select(root);

		criteria.where(qb.equal(root.get(BioBank_.id), id));

		if (loadTask)
			root.fetch(BioBank_.task, JoinType.LEFT);
		if (loadPDFs)
			root.fetch(BioBank_.attachedPdfs, JoinType.LEFT);

		criteria.distinct(true);
		List<BioBank> results = getSession().createQuery(criteria).getResultList();

		return Optional.ofNullable(results.size() > 0 ? results.get(0) : null);
	}

	public Optional<BioBank> findOptionalByTaskAndInitialize(Task task) {
		return findOptionalByTaskAndInitialize(task, false, true);
	}

	public Optional<BioBank> findOptionalByTaskAndInitialize(Task task, boolean loadTask, boolean loadPDFs) {
		CriteriaBuilder qb = getCriteriaBuilder();
		CriteriaQuery<BioBank> criteria = qb.createQuery(BioBank.class);
		Root<BioBank> root = criteria.from(BioBank.class);

		criteria.select(root);

		criteria.where(qb.equal(root.get(BioBank_.task), task));

		if (loadTask)
			root.fetch(BioBank_.task, JoinType.LEFT);
		if (loadPDFs)
			root.fetch(BioBank_.attachedPdfs, JoinType.LEFT);

		criteria.distinct(true);
		List<BioBank> results = getSession().createQuery(criteria).getResultList();

		return Optional.ofNullable(results.size() > 0 ? results.get(0) : null);
	}

}