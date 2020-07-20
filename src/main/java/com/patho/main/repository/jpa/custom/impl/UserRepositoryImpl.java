package com.patho.main.repository.jpa.custom.impl;

import com.patho.main.model.user.HistoUser;
import com.patho.main.model.user.HistoUser_;
import com.patho.main.repository.jpa.custom.UserRepositroyCustom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
@Transactional
public class UserRepositoryImpl extends AbstractRepositoryCustom implements UserRepositroyCustom {

    public List<HistoUser> findAllIgnoreArchived() {
        return findAllIgnoreArchived(true);
    }

    @Override
    public List<HistoUser> findAllIgnoreArchived(boolean irgnoreArchived) {
        CriteriaQuery<HistoUser> criteria = getCriteriaBuilder().createQuery(HistoUser.class);
        Root<HistoUser> root = criteria.from(HistoUser.class);

        criteria.select(root);
        if (irgnoreArchived)
            criteria.where(getCriteriaBuilder().equal(root.get(HistoUser_.archived), false));
        criteria.orderBy(getCriteriaBuilder().asc(root.get(HistoUser_.id)));
        criteria.distinct(true);

        return getSession().createQuery(criteria).getResultList();
    }
}
