package com.patho.main.util.dialog.event

import com.patho.main.common.ContactRole
import com.patho.main.model.PDFContainer
import com.patho.main.model.Physician
import com.patho.main.model.interfaces.ID
import com.patho.main.model.patient.DiagnosisRevision
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.model.person.Organization
import com.patho.main.model.user.HistoGroup
import com.patho.main.model.user.HistoUser
import com.patho.main.ui.selectors.StainingPrototypeHolder
import com.patho.main.util.ui.selector.ReportIntentSelector
import com.patho.main.util.worklist.Worklist
import java.io.Serializable

open class DialogReturnEvent : Serializable

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
 * Patient delete event
 */
class PatientDeleteEvent(patient: Patient) : SelectEvent<Patient>(patient)

/**
 * Event for selecting a new worklist
 */
class WorklistSelectEvent(worklist: Worklist) : SelectEvent<Worklist>(worklist)

/**
 * Event for selecting pdfs
 */
class PDFSelectEvent(pdf: PDFContainer) : SelectEvent<PDFContainer>(pdf)

/**
 * Event for removing the given patient from the worklist
 */
class RemovePatientFromWorklistEvent(patient: Patient) : SelectEvent<Patient>(patient)

/**
 * Return event for die ContactAddDialog dialog with a selected physician
 */
open class PhysicianSelectEvent(physician: Physician, val role: ContactRole = ContactRole.NONE, val isExtern: Boolean = false) : SelectEvent<Physician>(physician)

/**
 * Organization select event for the OrganizationListDialog
 */
open class OrganizationSelectEvent(organization: Organization) : SelectEvent<Organization>(organization)

/**
 * Delete event for ConfirmUserDeleteDialog
 */
open class HistoUserDeleteEvent(histoUser: HistoUser, val isDelete: Boolean, val isDeletePhysician: Boolean) : SelectEvent<HistoUser>(histoUser)

/**
 * Select event for AddSlidesDialog
 */
open class SlideSelectEvent(prototypes: List<StainingPrototypeHolder>) : SelectEvent<List<StainingPrototypeHolder>>(prototypes)

/**
 * Select event for the CustomAddressDialog
 */
// TODO test
// @java.beans.ConstructorProperties({"customAddress", "contactSelector"})
open class CustomAddressSelectEvent(val customAddress: String, contactSelector: ReportIntentSelector) : SelectEvent<ReportIntentSelector>(contactSelector)
//CustomAddressReturn

/**
 * Select event for the GroupListDialog
 */
open class GroupSelectEvent(histoGroup: HistoGroup) : SelectEvent<HistoGroup>(histoGroup)

/**
 * Select event for UserListDialog
 */
open class HistoUserSelectEvent(histoUser: HistoUser) : SelectEvent<HistoUser>(histoUser)

/**
 * Confirm event from ConfirmDialog
 */
open class ConfirmEvent(confirm: Boolean) : SelectEvent<Boolean>(confirm)

/**
 * Event for diagnosis creation
 */
open class QuickDiagnosisAddEvent(val diagnosisCreated: Boolean) : SelectEvent<Boolean>(diagnosisCreated)

/**
 * Reload event
 */
open class ReloadEvent : DialogReturnEvent()

/**
 * Task reload event
 */
open class TaskReloadEvent(var task: Task? = null) : ReloadEvent()

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
class UserReloadEvent(val user: HistoUser? = null) : ReloadEvent()

/**
 * Event after settings have been changed
 */
class SettingsReloadEvent : ReloadEvent()

/**
 * Return event for the StaininPhaseExitDialog
 */
class StainingPhaseExitEvent : TaskReloadEvent()

/**
 * Return event for the DiagnosisPhaseExitDialog
 */
class DiagnosisPhaseExitEvent(val task: Task, val revision: DiagnosisRevision?, val removeFromDiagnosisList: Boolean, val removeFromWorklist: Boolean, val performNotification: Boolean)

/**
 * Return event for the ExitNotification Dialog
 */
class NotificationPhaseExitEvent(val startTaskArchival: Boolean = false) : TaskReloadEvent()

/**
 * Return event for the NotificationPerforme dialog
 */
class NotificationPerformedEvent(val endPhase: Boolean) : TaskReloadEvent()

/**
 * Close Event for dialogs
 */
class DialogCloseEvent : ReloadEvent()