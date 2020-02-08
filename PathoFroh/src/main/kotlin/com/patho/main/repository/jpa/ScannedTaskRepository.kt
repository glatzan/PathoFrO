package com.patho.main.repository.jpa

import com.patho.main.model.preset.StainingPrototype
import com.patho.main.model.scanner.ScannedTask

interface ScannedTaskRepository : BaseRepository<ScannedTask, Long> {
}