package com.patho.main.action.views

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.ContactRole
import com.patho.main.model.patient.Sample
import com.patho.main.model.patient.Task
import com.patho.main.model.person.Person
import com.patho.main.model.preset.ListItem
import com.patho.main.model.preset.MaterialPreset
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.service.impl.SpringSessionContextBridge
import com.patho.main.util.bearer.SimplePhysicianBearer
import com.patho.main.util.ui.jsfcomponents.ISelectPhysicianOverlay

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


    open var surgeons = object : ISelectPhysicianOverlay {

        override val physicians: List<SimplePhysicianBearer>
            get() = SpringSessionContextBridge.services().genericViewData.surgeons

        override var selectedPhysician: SimplePhysicianBearer? = null

        override var filter: String = ""

        override fun onSelect() {
            val tmp = selectedPhysician
            if (tmp != null)
                addReportIntentToTask(tmp.physician.person, ContactRole.SURGEON)
        }
    }

    open var privatePhysician = object : ISelectPhysicianOverlay {

        override val physicians: List<SimplePhysicianBearer>
            get() = SpringSessionContextBridge.services().genericViewData.privatePhysicians

        override var selectedPhysician: SimplePhysicianBearer? = null

        override var filter: String = ""

        override fun onSelect() {
            val tmp = selectedPhysician
            if (tmp != null)
                addReportIntentToTask(tmp.physician.person, ContactRole.FAMILY_PHYSICIAN)
        }
    }

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
        SpringContextBridge.services().worklistHandler.replaceTaskInWorklist(task, true, false)
    }

    /**
     * Saves dynamic name changes of task entities
     */
    open fun save(task: Task, resourcesKey: String, vararg arr: Any) {
        logger.debug("Saving task " + task.taskID)
        val t = SpringContextBridge.services().taskRepository.save(task, resourceBundle.get(resourcesKey, task, *arr), task.patient)
        SpringContextBridge.services().worklistHandler.replaceTaskInWorklist(t, true, reloadStaticData = false)
    }

    /**
     * Updates the case history and saves the task
     */
    open fun updateCaseHistoryWithName(task: Task, caseHistory: String, resourcesKey: String, vararg arr: Any) {
        logger.debug("Updating case History setting to $caseHistory")
        task.caseHistory = caseHistory
        save(task, resourcesKey, *arr)
    }

    /**
     * Changes the material of the sample.
     */
    fun updateMaterialOfSample(sample: Sample, materialPreset: MaterialPreset?, materialPresetString: String, resourcesKey: String, vararg arr: Any) {
        logger.debug("Change material of sample with preset")
        val t = SpringContextBridge.services().sampleService.updateMaterialOfSample(sample, materialPresetString, materialPreset, false)
        save(t, resourcesKey, *arr)
    }
}