package com.patho.main.database;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.patho.main.model.user.HistoUser;
import com.patho.main.model.user.HistoUser_;

public interface RepositoryTest extends CrudRepository<HistoUser, Long>, JpaSpecificationExecutor<HistoUser> {

	public default List<HistoUser> findAll(boolean irgnoreArchived) {
		Specification<HistoUser> specification = new Specification<HistoUser>() {
			public Predicate toPredicate(Root<HistoUser> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get(HistoUser_.archived), irgnoreArchived));
				return builder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
		
		return findAll(specification);
	}
}
