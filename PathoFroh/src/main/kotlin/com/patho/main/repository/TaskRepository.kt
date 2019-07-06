package com.patho.main.repository

import com.patho.main.model.patient.Task
import com.patho.main.repository.custom.TaskRepositoryCustom
import java.util.*

interface TaskRepository : BaseRepository<Task, Long>, TaskRepositoryCustom {
    fun findOptionalById(id: Long?): Optional<Task>
}
