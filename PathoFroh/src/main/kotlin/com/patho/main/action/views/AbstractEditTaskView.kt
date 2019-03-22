package com.patho.main.action.views

import com.patho.main.action.handler.CentralHandler
import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.ContactRole
import com.patho.main.model.ListItem
import com.patho.main.model.MaterialPreset
import com.patho.main.model.patient.Task
import com.patho.main.model.person.Person
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.ui.selectors.PhysicianSelector
import com.patho.main.util.bearer.SimplePhysicianBearer

abstract class AbstractEditTaskView : AbstractTaskView() {

    /**
     * Search string for case history
     */
    open var caseHistoryFilter: String = ""

    /**
     * Selected List item form caseHistory list
     */
    open var selectedCaseHistoryItem: ListItem? = null

    /**
     * Selected material presets
     */
    open var selectedMaterialPresets: Array<MaterialPreset?> = arrayOf<MaterialPreset?>()

    /**
     * Material preset filter
     */
    open var selectedMaterialPresetFilter: Array<String> = arrayOf<String>()

    /**
     * Selected surgeon
     */
    open var selectedSurgeon: SimplePhysicianBearer? = null

    /**
     * Surgeon filter
     */
    open var selectedSurgeonFilter: String = ""

    /**
     * Private physician surgeon
     */
    open var selectedPrivatePhysician: SimplePhysicianBearer? = null

    /**
     * Private physician filter
     */
    open var selectedPrivatePhysicianFilter: String = ""

    /**
     * Resets all filters an select fields
     */
    override fun loadView(task: Task) {
        super.loadView(task)

        selectedCaseHistoryItem = null
        caseHistoryFilter = ""

        selectedMaterialPresets = Array<MaterialPreset?>(task.samples.size) { _ -> null }
        selectedMaterialPresetFilter = Array<String>(task.samples.size) { _ -> "" }

        selectedSurgeon = null
        selectedSurgeonFilter = ""

        selectedPrivatePhysician = null
        selectedPrivatePhysicianFilter = ""
    }

    /**
     * Adds a person as a report intent to the selected task
     */
    fun addReportIntentToTask(person: Person, role: ContactRole) {
        try {
            SpringContextBridge.services().reportIntentService.addReportIntent(task, person, role, true, true)
            // increment counter
            SpringContextBridge.services().physicianService.incrementPhysicianPriorityCounter(person)
        } catch (e: IllegalArgumentException) {
            // todo error message
            logger.debug("Not adding, double contact")
            MessageHandler.sendGrowlMessagesAsResource("growl.error", "growl.error.contact.duplicated")
        }

        // reloading task
        SpringContextBridge.services().centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK)
    }
}