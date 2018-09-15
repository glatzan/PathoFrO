package com.patho.main.repository.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.common.ContactRole;
import com.patho.main.common.SortOrder;
import com.patho.main.model.Person;
import com.patho.main.model.Person_;
import com.patho.main.model.Physician;
import com.patho.main.model.Physician_;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.service.PhysicianRepositoryCustom;
import com.patho.main.ui.selectors.PhysicianSelector;

@Service
@Transactional
public class PhysicianRepositoryImpl extends AbstractRepositoryCustom implements PhysicianRepositoryCustom {

	@Override
	public List<Physician> findAllByRole(ContactRole role, boolean irgnoreArchived) {
		return findAllByRole(Arrays.asList(role), irgnoreArchived, SortOrder.NAME);
	}

	@Override
	public List<Physician> findAllByRole(List<ContactRole> roles, boolean irgnoreArchived) {
		return findAllByRole(roles, irgnoreArchived, SortOrder.NAME);
	}

	@Override
	public List<Physician> findAllByRole(ContactRole[] roles, boolean irgnoreArchived) {
		return findAllByRole(roles, irgnoreArchived, SortOrder.NAME);
	}

	@Override
	public List<Physician> findAllByRole(ContactRole[] roles, boolean irgnoreArchived, SortOrder sortOrder) {
		return findAllByRole(Arrays.asList(roles), irgnoreArchived, sortOrder);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Physician> findAllByRole(List<ContactRole> roles, boolean irgnoreArchived, SortOrder sortOrder) {
		CriteriaQuery<Physician> criteria = getCriteriaBuilder().createQuery(Physician.class);
		Root<Physician> root = criteria.from(Physician.class);

		criteria.select(root);

		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(root.join(Physician_.associatedRoles, JoinType.LEFT).in(roles));

		if (irgnoreArchived)
			predicates.add(getCriteriaBuilder().equal(root.get(Physician_.archived), false));

		criteria.where(getCriteriaBuilder().and(predicates.toArray(new Predicate[predicates.size()])));

		Path<Person> fetchAsPath = (Path<Person>) root.fetch(Physician_.person, JoinType.LEFT);

		if (sortOrder == SortOrder.NAME) {
			criteria.orderBy(getCriteriaBuilder().asc(fetchAsPath.get(Person_.lastName)));
		} else if (sortOrder == SortOrder.PRIORITY)
			criteria.orderBy(getCriteriaBuilder().desc(root.get(Physician_.priorityCount)),
					getCriteriaBuilder().asc(fetchAsPath.get(Person_.lastName)));
		else
			criteria.orderBy(getCriteriaBuilder().asc(root.get(Physician_.id)));

		criteria.distinct(true);

		return getSession().createQuery(criteria).getResultList();
	}

	@Override
	public List<PhysicianSelector> findSelectorsByRole(Task task, ContactRole roles, SortOrder sortOrder) {
		return findSelectorsByRole(task, Arrays.asList(roles), sortOrder);
	}

	@Override
	public List<PhysicianSelector> findSelectorsByRole(Task task, ContactRole[] roles, SortOrder sortOrder) {
		return findSelectorsByRole(task, Arrays.asList(roles), sortOrder);
	}

	@Override
	public List<PhysicianSelector> findSelectorsByRole(Task task, List<ContactRole> roles, SortOrder sortOrder) {
		List<Physician> databasePhysicians = findAllByRole(roles, true, sortOrder);
		logger.debug(databasePhysicians.size() + " " + roles.size());
		return PhysicianSelector.factory(task, databasePhysicians);
	}
}
