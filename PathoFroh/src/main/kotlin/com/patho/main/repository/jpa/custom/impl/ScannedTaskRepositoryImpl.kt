package com.patho.main.repository.jpa.custom.impl

import com.patho.main.model.preset.MaterialPreset_
import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.preset.StainingPrototype_
import com.patho.main.model.scanner.ScannedTask
import com.patho.main.model.scanner.ScannedTask_
import com.patho.main.repository.jpa.custom.ScannedTaskRepositoryCustom
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.JoinType

@Service
open class ScannedTaskRepositoryImpl  : AbstractRepositoryCustom(), ScannedTaskRepositoryCustom  {

    @Transactional
    override fun findOptionalByIdAndInitializeTask(id: Long): Optional<ScannedTask> {
        val criteria: CriteriaQuery<ScannedTask> = criteriaBuilder.createQuery(ScannedTask::class.java)
        val root = criteria.from(ScannedTask::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(ScannedTask_.id), id))
        criteria.distinct(true)
        root.fetch(ScannedTask_.task, JoinType.LEFT)
        val scannedTasks: List<ScannedTask> = session.createQuery(criteria).resultList
        return Optional.ofNullable(if (scannedTasks.isNotEmpty()) scannedTasks[0] else null)
    }
}