package com.patho.main.repository.jpa.custom.impl

import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.preset.StainingPrototypeType
import com.patho.main.model.preset.StainingPrototype_
import com.patho.main.repository.jpa.custom.StainingPrototypeRepositoryCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Predicate

@Service
open class StainingPrototypeRepositoryImpl : AbstractRepositoryCustom(), StainingPrototypeRepositoryCustom {

    @Transactional
    override fun findOptionalByIdAndInitialize(id: Long): Optional<StainingPrototype> {
        val criteria: CriteriaQuery<StainingPrototype> = criteriaBuilder.createQuery(StainingPrototype::class.java)
        val root = criteria.from(StainingPrototype::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(StainingPrototype_.id), id))
        criteria.distinct(true)
        val stainingPrototypes: List<StainingPrototype> = session.createQuery(criteria).resultList
        return Optional.ofNullable(if (stainingPrototypes.isNotEmpty()) stainingPrototypes[0] else null)
    }

    @Transactional
    override fun findAllByTypeIgnoreArchivedOrderByPriorityCountDesc(type: StainingPrototypeType, ignoreArchived: Boolean): List<StainingPrototype> {
        val criteria: CriteriaQuery<StainingPrototype> = criteriaBuilder.createQuery(StainingPrototype::class.java)
        val root = criteria.from(StainingPrototype::class.java)
        criteria.select(root)
        val predicates: MutableList<Predicate> = ArrayList()
        predicates.add(criteriaBuilder.equal(root.get(StainingPrototype_.type), type))
        if (ignoreArchived) predicates.add(criteriaBuilder.equal(root.get(StainingPrototype_.archived), false))
        criteria.where(criteriaBuilder.and(*predicates.toTypedArray()))
        criteria.orderBy(criteriaBuilder.desc(root.get(StainingPrototype_.priorityCount)))
        criteria.distinct(true)
        return getSession().createQuery(criteria).resultList
    }
}