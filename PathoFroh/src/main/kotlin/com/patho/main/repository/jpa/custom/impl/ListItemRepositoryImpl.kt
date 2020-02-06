package com.patho.main.repository.jpa.custom.impl

import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.ListItemType
import com.patho.main.model.preset.ListItem_
import com.patho.main.repository.jpa.custom.ListItemRepositoryCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.criteria.Predicate

@Service
open class ListItemRepositoryImpl : AbstractRepositoryCustom(), ListItemRepositoryCustom {

    @Transactional
    override fun findAll(listType: ListItemType, ignoreArchived: Boolean): List<ListItem> {
        val criteria = criteriaBuilder.createQuery(ListItem::class.java)
        val root = criteria.from(ListItem::class.java)
        criteria.select(root)
        val predicates: MutableList<Predicate> = ArrayList()
        predicates.add(criteriaBuilder.equal(root.get(ListItem_.listType), listType))
        if (ignoreArchived) predicates.add(criteriaBuilder.equal(root.get(ListItem_.archived), false))
        criteria.where(criteriaBuilder.and(*predicates.toTypedArray()))
        criteria.distinct(true)
        return session.createQuery(criteria).resultList
    }

    @Transactional
    override fun findAllOrderByIndex(listType: ListItemType, ignoreArchived: Boolean): List<ListItem> {
        val criteria = criteriaBuilder.createQuery(ListItem::class.java)
        val root = criteria.from(ListItem::class.java)
        criteria.select(root)
        val predicates: MutableList<Predicate> = ArrayList()
        predicates.add(criteriaBuilder.equal(root.get(ListItem_.listType), listType))
        if (ignoreArchived) predicates.add(criteriaBuilder.equal(root.get(ListItem_.archived), false))
        criteria.where(criteriaBuilder.and(*predicates.toTypedArray()))
        criteria.orderBy(criteriaBuilder.asc(root.get(ListItem_.indexInList)))
        criteria.distinct(true)
        return session.createQuery(criteria).resultList
    }
}