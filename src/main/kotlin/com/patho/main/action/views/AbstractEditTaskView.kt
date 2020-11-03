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
import com.patho.main.util.ui.jsfcomponents.*

abstract class AbstractEditTaskView : AbstractTaskView(), IMaterialSelectComponent, ICaseHistoryComponent {

    /**
     * Object for selecting new surgeons quickly by overlay
     */
    open val surgeons = object : IPhysicianSelectOverlay {

        override val physicians: List<SimplePhysicianBearer>
            get() {
                return if (SpringContextBridge.services().currentUserHandler.currentUser.settings.surgeonSortOrder)
                    SpringSessionContextBridge.services().genericViewData.surgeons.sortedBy { it.physician.person.lastName }
                else
                    SpringSessionContextBridge.services().genericViewData.surgeons.sortedByDescending { it.physician.priorityCount }
            }

        override var selectedPhysician: SimplePhysicianBearer? = null

        override var filter: String = ""

        override fun onSelect() {
            val tmp = selectedPhysician
            if (tmp != null)
                addReportIntentToTask(tmp.physician.person, ContactRole.SURGEON)
        }
    }

    /**
     * Object for selecting new private physicians quickly by overlay
     */
    open val privatePhysician = object : IPhysicianSelectOverlay {

        override val physicians: List<SimplePhysicianBearer>
            get() {
                return if (SpringContextBridge.services().currentUserHandler.currentUser.settings.surgeonSortOrder)
                    SpringSessionContextBridge.services().genericViewData.privatePhysicians.sortedBy { it.physician.person.lastName }
                else
                    SpringSessionContextBridge.services().genericViewData.privatePhysicians.sortedByDescending { it.physician.priorityCount }
            }

        override var selectedPhysician: SimplePhysicianBearer? = null

        override var filter: String = ""

        override fun onSelect() {
            val tmp = selectedPhysician
            if (tmp != null)
                addReportIntentToTask(tmp.physician.person, ContactRole.PRIVATE_PHYSICIAN)
        }

        override fun onShow() {
            this.filter = ""
            this.selectedPhysician = null
        }
    }

    /**
     * Object for changing the case history by overlay
     */
    override val caseHistory = object : ICaseHistorySelectOverlay {

        override var selectedItem: ListItem? = null

        override val items: List<ListItem>
            get() = SpringSessionContextBridge.services().genericViewData.caseHistoryList

        override var filter: String = ""

        override fun onShow() {
            this.filter = ""
            this.selectedItem = null
        }
    }

    /**
     * Object for changing the material by overlay
     */
    override val material = object : IMaterialSelectOverlay {

        override var selectedMaterial: MaterialPreset? = null

        override val presets: List<MaterialPreset>
            get() = SpringSessionContextBridge.services().genericViewData.materialList

        override var filter: String = ""

        override var parentSample: Sample? = null

        override fun onShow() {
            this.filter = ""
            this.selectedMaterial = null
        }

    }

    /**
     * Resets all filters an select fields
     */
    override fun loadView(task: Task) {
        super.loadView(task)
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
    override fun save(task: Task, resourcesKey: String, vararg arr: Any) {
        logger.debug("Saving task " + task.taskID)
        val t = SpringContextBridge.services().taskRepository.save(task, resourceBundle[resourcesKey, task, arr], task.patient)
        SpringContextBridge.services().worklistHandler.replaceTaskInWorklist(t, true, reloadStaticData = false)
    }

    /**
     * Updates the case history and saves the task
     */
    override fun updateCaseHistoryWithName(task: Task, caseHistory: String, resourcesKey: String, vararg arr: Any) {
        logger.debug("Updating case History setting to $caseHistory")
        task.caseHistory = caseHistory
        save(task, resourcesKey, *arr)
    }

    /**
     * Changes the material of the sample.
     */
    override fun updateMaterialOfSample(sample: Sample, materialPreset: MaterialPreset?, materialPresetString: String, resourcesKey: String, vararg arr: Any) {
        logger.debug("Change material of sample with preset")
        val t = SpringContextBridge.services().sampleService.updateMaterialOfSample(sample, materialPresetString, materialPreset, false)
        save(t, resourcesKey, *arr)
    }
}
