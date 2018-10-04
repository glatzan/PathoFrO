package com.patho.main.repository.service.impl;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.Organization;
import com.patho.main.model.Organization_;
import com.patho.main.repository.service.OrganizationRepositoryCustom;

@Service
@Transactional
public class OrganizationRepositoryImpl extends AbstractRepositoryCustom implements OrganizationRepositoryCustom {

	@Override
	public Optional<Organization> findOptionalByNameAndInitializePersons(String name) {
		CriteriaQuery<Organization> criteria = getCriteriaBuilder().createQuery(Organization.class);
		Root<Organization> root = criteria.from(Organization.class);

		criteria.select(root);

		root.fetch(Organization_.persons, JoinType.LEFT);

		criteria.where(getCriteriaBuilder().equal(root.get(Organization_.name), name));

		criteria.distinct(true);

		List<Organization> organizations = getSession().createQuery(criteria).getResultList();

		return Optional.ofNullable(!organizations.isEmpty() ? organizations.get(0) : null);
	}

	@Override
	public List<Organization> findAll(boolean irgnoreArchived) {
		return findAll(false, irgnoreArchived);
	}

	@Override
	public List<Organization> findAll(boolean loadPersons, boolean irgnoreArchived) {
		CriteriaQuery<Organization> criteria = getCriteriaBuilder().createQuery(Organization.class);
		Root<Organization> root = criteria.from(Organization.class);

		criteria.select(root);

		if (loadPersons)
			root.fetch(Organization_.persons, JoinType.LEFT);

		if (irgnoreArchived)
			criteria.where(getCriteriaBuilder().equal(root.get(Organization_.archived), false));

		criteria.distinct(true);

		return getSession().createQuery(criteria).getResultList();
	}

	@Override
	public List<Organization> findAllOrderByIdAsc(boolean irgnoreArchived) {
		return findAllOrderByIdAsc(false, irgnoreArchived);
	}

	@Override
	public List<Organization> findAllOrderByIdAsc(boolean loadPersons, boolean irgnoreArchived) {
		CriteriaQuery<Organization> criteria = getCriteriaBuilder().createQuery(Organization.class);
		Root<Organization> root = criteria.from(Organization.class);

		criteria.select(root);

		criteria.orderBy(getCriteriaBuilder().asc(root.get(Organization_.id)));

		if (loadPersons)
			root.fetch(Organization_.persons, JoinType.LEFT);

		if (irgnoreArchived)
			criteria.where(getCriteriaBuilder().equal(root.get(Organization_.archived), false));

		criteria.distinct(true);

		return getSession().createQuery(criteria).getResultList();
	}

}
