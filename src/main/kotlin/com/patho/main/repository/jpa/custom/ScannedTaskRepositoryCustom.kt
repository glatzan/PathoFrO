package com.patho.main.repository.jpa.custom

import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.scanner.ScannedTask
import java.util.*

interface ScannedTaskRepositoryCustom {
    fun findOptionalByIdAndInitializeTask(id: Long): Optional<ScannedTask>
}