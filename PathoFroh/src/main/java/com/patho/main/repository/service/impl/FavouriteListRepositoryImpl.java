package com.patho.main.repository.service.impl;

import com.patho.main.model.dto.FavouriteListMenuItem;
import com.patho.main.model.favourites.*;
import com.patho.main.model.patient.Task;
import com.patho.main.model.user.HistoUser;
import com.patho.main.repository.service.FavouriteListRepositoryCustom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FavouriteListRepositoryImpl extends AbstractRepositoryCustom
		implements FavouriteListRepositoryCustom {

	public Optional<FavouriteList> findOptionalByIdAndInitialize(Long id, boolean loadList, boolean loadPermissions,
			boolean loadDumpList) {
		return findOptionalByIdAndInitialize(id, loadList, loadList, loadPermissions, loadDumpList);
	}

	public Optional<FavouriteList> findOptionalByIdAndInitialize(Long id, boolean loadItems, boolean loadOwner,
			boolean loadPermissions, boolean loadDumpList) {
		CriteriaBuilder qb = getCriteriaBuilder();
		CriteriaQuery<FavouriteList> criteria = qb.createQuery(FavouriteList.class);
		Root<FavouriteList> root = criteria.from(FavouriteList.class);

		criteria.select(root);

		criteria.where(qb.equal(root.get(FavouriteList_.id), id));

		if (loadItems)
			root.fetch(FavouriteList_.items, JoinType.LEFT);

		if (loadOwner)
			root.fetch(FavouriteList_.owner, JoinType.LEFT);

		if (loadPermissions) {
			root.fetch(FavouriteList_.users, JoinType.LEFT);
			root.fetch(FavouriteList_.groups, JoinType.LEFT);
		}

		if (loadDumpList)
			root.fetch(FavouriteList_.dumpList, JoinType.LEFT);

		criteria.distinct(true);

		List<FavouriteList> groups = getSession().createQuery(criteria).getResultList();

		return Optional.ofNullable(groups.size() > 0 ? groups.get(0) : null);
	}

	public List<FavouriteList> findByUserAndWriteableAndReadable(HistoUser user, boolean writeable, boolean readable) {
		return findByUserAndWriteableAndReadable(user, writeable, readable, false, false, false);
	}

	public List<FavouriteList> findByUserAndWriteableAndReadable(HistoUser user, boolean writeable, boolean readable,
			boolean loadList, boolean loadPermissions, boolean loadDumpList) {
		return findByUserAndWriteableAndReadable(user, writeable, readable, loadList, loadList, loadPermissions,
				loadDumpList);
	}

	public List<FavouriteList> findByUserAndWriteableAndReadable(HistoUser user, boolean writeable, boolean readable,
			boolean loadItems, boolean loadOwner, boolean loadPermissions, boolean loadDumpList) {
		// Create CriteriaBuilder
		CriteriaBuilder qb = getSession().getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<FavouriteList> criteria = qb.createQuery(FavouriteList.class);
		Root<FavouriteList> root = criteria.from(FavouriteList.class);
		criteria.select(root);

		Join<FavouriteList, FavouritePermissionsUser> userQuery = root.join(FavouriteList_.users, JoinType.LEFT);
		Join<FavouriteList, FavouritePermissionsGroup> groupQuery = root.join(FavouriteList_.groups, JoinType.LEFT);

		if (loadItems)
			root.fetch(FavouriteList_.items, JoinType.LEFT);

		if (loadOwner)
			root.fetch(FavouriteList_.owner, JoinType.LEFT);

		if (loadPermissions) {
			root.fetch(FavouriteList_.users, JoinType.LEFT);
			root.fetch(FavouriteList_.groups, JoinType.LEFT);
		}

		if (loadDumpList)
			root.fetch(FavouriteList_.dumpList, JoinType.LEFT);

		Predicate andUser = null;
		Predicate andGroup = null;

		if (writeable && readable) {
			andUser = qb.and(qb.equal(userQuery.get(FavouritePermissionsUser_.readable), true),
					qb.equal(userQuery.get(FavouritePermissionsUser_.editable), true),
					qb.equal(userQuery.get(FavouritePermissionsUser_.user), user.getId()));
			andGroup = qb.and(qb.equal(groupQuery.get(FavouritePermissionsGroup_.readable), true),
					qb.equal(groupQuery.get(FavouritePermissionsGroup_.editable), true),
					qb.equal(groupQuery.get(FavouritePermissionsGroup_.group), user.getGroup().getId()));
		} else if (writeable) {
			andUser = qb.and(qb.equal(userQuery.get(FavouritePermissionsUser_.editable), true),
					qb.equal(userQuery.get(FavouritePermissionsUser_.user), user.getId()));
			andGroup = qb.and(qb.equal(groupQuery.get(FavouritePermissionsGroup_.editable), true),
					qb.equal(groupQuery.get(FavouritePermissionsGroup_.group), user.getGroup().getId()));
		} else if (readable) {
			andUser = qb.and(qb.equal(userQuery.get(FavouritePermissionsUser_.readable), true),
					qb.equal(userQuery.get(FavouritePermissionsUser_.user), user.getId()));
			andGroup = qb.and(qb.equal(groupQuery.get(FavouritePermissionsGroup_.readable), true),
					qb.equal(groupQuery.get(FavouritePermissionsGroup_.group), user.getGroup().getId()));
		} else {
			andUser = qb.equal(userQuery.get(FavouritePermissionsUser_.user), user.getId());
			andGroup = qb.equal(groupQuery.get(FavouritePermissionsGroup_.group), user.getGroup().getId());
		}

		Predicate orClause = qb.or(qb.equal(root.get(FavouriteList_.owner), user),
				qb.equal(root.get(FavouriteList_.globalView), true), andUser, andGroup);

		criteria.where(orClause);

		criteria.distinct(true);

		List<FavouriteList> favouriteLists = getSession().createQuery(criteria).getResultList();

		return favouriteLists;
	}

	public List<FavouriteList> findAll() {
		return findAll(false, false, false, false);
	}

	public List<FavouriteList> findAll(boolean loadDumpList) {
		return findAll(false, false, false, loadDumpList);
	}

	public List<FavouriteList> findAll(boolean loadItems, boolean loadOwner, boolean loadPermissions,
			boolean loadDumpList) {

		CriteriaBuilder qb = getSession().getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<FavouriteList> criteria = qb.createQuery(FavouriteList.class);
		Root<FavouriteList> root = criteria.from(FavouriteList.class);
		criteria.select(root);

		criteria.orderBy(qb.asc(root.get("id")));

		if (loadItems)
			root.fetch(FavouriteList_.items, JoinType.LEFT);

		if (loadOwner)
			root.fetch(FavouriteList_.owner, JoinType.LEFT);

		if (loadPermissions) {
			root.fetch(FavouriteList_.users, JoinType.LEFT);
			root.fetch(FavouriteList_.groups, JoinType.LEFT);
		}

		if (loadDumpList)
			root.fetch(FavouriteList_.dumpList, JoinType.LEFT);

		criteria.distinct(true);
		
		List<FavouriteList> lists = getSession().createQuery(criteria).getResultList();

		return lists;
	}

	public List<FavouriteList> findByIds(List<Long> ids) {
		CriteriaBuilder qb = getSession().getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<FavouriteList> criteria = qb.createQuery(FavouriteList.class);
		Root<FavouriteList> root = criteria.from(FavouriteList.class);
		criteria.select(root);

		Expression<Long> exp = root.get(FavouriteList_.id);
		Predicate predicate = exp.in(ids);
		criteria.where(predicate);

		List<FavouriteList> lists = getSession().createQuery(criteria).getResultList();

		return lists;
	}

	@SuppressWarnings("unchecked")
	public List<FavouriteListMenuItem> getMenuItems(HistoUser user, Task task) {
		List<FavouriteListMenuItem> items = getSession().createNamedQuery("favouriteListMenuItemDTO")
				.setParameter("user_id", user.getId()).setParameter("group_id", user.getGroup().getId())
				.setParameter("task_id", task.getId()).getResultList();

		return items;
	}

}
