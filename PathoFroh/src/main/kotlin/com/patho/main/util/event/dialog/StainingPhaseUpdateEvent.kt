package com.patho.main.util.event.dialog

import com.patho.main.model.patient.Task
import com.patho.main.util.dialogReturn.ReloadEvent

/**
 * Return event with a staining phase update
 */
class StainingPhaseUpdateEvent(val task: Task) : ReloadEvent()