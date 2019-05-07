package com.patho.main.util.event.dialog

import com.patho.main.model.interfaces.ID
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.user.HistoUser
import com.patho.main.util.worklist.Worklist

open class DialogReturnEvent

/**
 * Select event
 */
open class SelectEvent<T>(val obj: T) : DialogReturnEvent()

/**
 * Select event for external patients
 */
class PatientSelectEvent(patient: Patient) : SelectEvent<Patient>(patient)

/**
 * Event with selected entity to delete
 */
class TaskEntityDeleteEvent(entity: ID) : SelectEvent<ID>(entity)

/**
 * Event for selecting a new worklist
 */
class WorklistSelectEvent(worklist: Worklist) : SelectEvent<Worklist>(worklist)

/**
 * Reload event
 */
open class ReloadEvent : DialogReturnEvent()

/**
 * Task reload event
 */
class TaskReloadEvent(var task: org.apache.tools.ant.Task) : ReloadEvent()

/**
 * Reload event for patient.
 */
class PatientReloadEvent(val patient: Patient, val task: Task? = null, val select: Boolean = false) : ReloadEvent()

/**
 * Merge and reload event on patient merge
 */
class PatientMergeEvent(val source: Patient, val target: Patient) : ReloadEvent()

/**
 * Return event with a staining phase update
 */
class StainingPhaseUpdateEvent(val task: Task) : ReloadEvent()

/**
 * Event on user data change
 */
class UserReloadEvent(val user: HistoUser) : ReloadEvent()

/**
 * Return event for the StaininPhaseExitDialog
 */
class StainingPhaseExitEvent(val task: Task, val removeFromWorklist: Boolean = false, val removeFromStainingList: Boolean, val endStainingPhase: Boolean, val gotToDiagnosisPhase: Boolean)