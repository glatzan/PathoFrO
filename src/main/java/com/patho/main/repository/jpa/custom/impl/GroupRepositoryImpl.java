package com.patho.main.repository.jpa.custom.impl;

import com.patho.main.model.user.HistoGroup;
import com.patho.main.model.user.HistoGroup_;
import com.patho.main.repository.jpa.custom.GroupRepositoryCustom;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupRepositoryImpl extends AbstractRepositoryCustom implements GroupRepositoryCustom {

    public void initializeGroupSettings(HistoGroup group) {
        Hibernate.initialize(group.getSettings());
    }

    @Override
    public Optional<HistoGroup> findOptionalById(Long id, boolean loadSettings) {
        CriteriaBuilder qb = getCriteriaBuilder();
        CriteriaQuery<HistoGroup> criteria = qb.createQuery(HistoGroup.class);
        Root<HistoGroup> root = criteria.from(HistoGroup.class);

        criteria.select(root);

        criteria.where(qb.equal(root.get(HistoGroup_.id), id));

        if (loadSettings)
            root.fetch(HistoGroup_.settings, JoinType.LEFT);

        criteria.distinct(true);

        List<HistoGroup> groups = getSession().createQuery(criteria).getResultList();

        return Optional.ofNullable(groups.size() > 0 ? groups.get(0) : null);
    }

    @Override
    public List<HistoGroup> findAll(boolean irgnoreArchived) {
        return findAll(false, irgnoreArchived);
    }

    @Override
    public List<HistoGroup> findAll(boolean initilizeSettings, boolean irgnoreArchived) {
        CriteriaBuilder qb = getCriteriaBuilder();
        CriteriaQuery<HistoGroup> criteria = qb.createQuery(HistoGroup.class);
        Root<HistoGroup> root = criteria.from(HistoGroup.class);

        criteria.select(root);

        if (irgnoreArchived)
            criteria.where(qb.equal(root.get(HistoGroup_.archived), false));

        if (initilizeSettings)
            root.fetch(HistoGroup_.settings, JoinType.LEFT);

        criteria.distinct(true);

        return getSession().createQuery(criteria).getResultList();
    }

    @Override
    public List<HistoGroup> findAllOrderByIdAsc() {
        return findAllOrderByIdAsc(true);
    }

    @Override
    public List<HistoGroup> findAllOrderByIdAsc(boolean irgnoreArchived) {
        return findAllOrderByIdAsc(false, irgnoreArchived);
    }

    @Override
    public List<HistoGroup> findAllOrderByIdAsc(boolean initilizeSettings, boolean irgnoreArchived) {
        CriteriaBuilder qb = getCriteriaBuilder();
        CriteriaQuery<HistoGroup> criteria = qb.createQuery(HistoGroup.class);
        Root<HistoGroup> root = criteria.from(HistoGroup.class);

        criteria.select(root);
        if (irgnoreArchived)
            criteria.where(qb.equal(root.get(HistoGroup_.archived), false));
        criteria.orderBy(qb.asc(root.get(HistoGroup_.id)));

        if (initilizeSettings)
            root.fetch(HistoGroup_.settings, JoinType.LEFT);

        criteria.distinct(true);

        return getSession().createQuery(criteria).getResultList();

    }

}
