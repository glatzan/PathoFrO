package com.patho.main.repository.jpa.custom.impl

import com.patho.main.model.preset.MaterialPreset
import com.patho.main.model.preset.MaterialPreset_
import com.patho.main.repository.jpa.custom.MaterialPresetRepositoryCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.criteria.JoinType

@Service
open class MaterialPresetRepositoryImpl : AbstractRepositoryCustom(), MaterialPresetRepositoryCustom {

    @Transactional
    override fun findAll(loadStainings: Boolean): List<MaterialPreset> {
        val criteria = criteriaBuilder.createQuery(MaterialPreset::class.java)
        val root = criteria.from(MaterialPreset::class.java)
        criteria.select(root)
        if (loadStainings) root.fetch(MaterialPreset_.stainingPrototypes, JoinType.LEFT)
        criteria.distinct(true)
        return session.createQuery(criteria).resultList
    }

    @Transactional
    override fun findAllByName(name: String, loadStainings: Boolean): List<MaterialPreset> {
        val criteria = criteriaBuilder.createQuery(MaterialPreset::class.java)
        val root = criteria.from(MaterialPreset::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.like(criteriaBuilder.lower(root.get(MaterialPreset_.name)),
                name.toLowerCase()))
        if (loadStainings) root.fetch(MaterialPreset_.stainingPrototypes, JoinType.LEFT)
        criteria.distinct(true)
        return session.createQuery(criteria).resultList
    }

    @Transactional
    override fun findAllOrderByPriorityCountDesc(loadStainings: Boolean,
                                                 irgnoreArchived: Boolean): List<MaterialPreset> {
        val criteria = criteriaBuilder.createQuery(MaterialPreset::class.java)
        val root = criteria.from(MaterialPreset::class.java)
        criteria.select(root)
        if (loadStainings) root.fetch(MaterialPreset_.stainingPrototypes, JoinType.LEFT)
        if (irgnoreArchived) criteria.where(criteriaBuilder.equal(root.get(MaterialPreset_.archived), false))
        criteria.orderBy(criteriaBuilder.desc(root.get(MaterialPreset_.priorityCount)))
        criteria.distinct(true)
        return session.createQuery(criteria).resultList
    }

    @Transactional
    override fun findAllOrderByIndexInListAsc(loadStainings: Boolean,
                                                irgnoreArchived: Boolean) : List<MaterialPreset>{
        val criteria = criteriaBuilder.createQuery(MaterialPreset::class.java)
        val root = criteria.from(MaterialPreset::class.java)
        criteria.select(root)
        if (loadStainings) root.fetch(MaterialPreset_.stainingPrototypes, JoinType.LEFT)
        if (irgnoreArchived) criteria.where(criteriaBuilder.equal(root.get(MaterialPreset_.archived), false))
        criteria.orderBy(criteriaBuilder.asc(root.get(MaterialPreset_.indexInList)))
        criteria.distinct(true)
        return session.createQuery(criteria).resultList
    }
}