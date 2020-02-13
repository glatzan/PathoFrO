package com.patho.main.repository.jpa

import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.scanner.ScannedTask
import com.patho.main.repository.jpa.custom.ScannedTaskRepositoryCustom

interface ScannedTaskRepository : BaseRepository<ScannedTask, Long>, ScannedTaskRepositoryCustom {
}