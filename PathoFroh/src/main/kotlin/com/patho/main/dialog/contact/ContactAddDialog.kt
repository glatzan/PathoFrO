package com.patho.main.dialog.contact

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.common.SortOrder
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.Physician
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.repository.PhysicianRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.service.ReportIntentService
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.dialog.event.PhysicianSelectEvent
import com.patho.main.util.exceptions.TaskNotFoundException
import com.patho.main.util.ui.selector.PhysicianSelector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class ContactAddDialog @Autowired constructor(
        private val physicianRepository: PhysicianRepository,
        private val reportIntentService: ReportIntentService,
        private val taskRepository: TaskRepository
) : AbstractTaskDialog(Dialog.CONTACTS_SELECT) {

    /**
     * Array of roles for that physicians should be shown.
     */
    open var showRoles = arrayOf<ContactRole>()

    /**
     * List of all ContactRole available for selecting physicians.
     */
    open var selectAbleRoles = arrayOf<ContactRole>()

    /**
     * Array of roles which can be added
     */
    open var addableRoles = arrayOf<ContactRole>()

    /**
     * Role of the quick associatedContact select dialog, either SURGEON or
     * PRIVATE_PHYSICIAN
     */
    open var addContactAsRole: ContactRole? = ContactRole.NONE

    /**
     * List contain contacts to select from, used by contacts
     */
    open var contactList = listOf<ContactPhysicianSelector>()

    /**
     * For quickContact selection
     */
    open var selectedContact: ContactPhysicianSelector? = null

    /**
     * If true the user can change the role with that the physician is added
     */
    open var manuallySelectRole = false

    /**
     * If true the role to add an physician will be determined by the physicians
     * roles matching the addableRoles. If there are several matching roles the
     * first matching role will be used.
     */
    open var autoRoleSelection = false

    /**
     * Returns true if the physician can be selected
     */
    open val isPhysicianSelectable
        get() = selectedContact != null && !(selectedContact?.contactOfTask ?: true)


    fun initAndPrepareBean(task: Task, selectAbleRoles: Array<ContactRole> = ContactRole.values(),
                           showRoles: Array<ContactRole> = ContactRole.values(),
                           addableRoles: Array<ContactRole> = ContactRole.values(),
                           addContactAsRole: ContactRole = ContactRole.NONE,
                           manuallySelectRole: Boolean = false,
                           autoRoleSelection: Boolean = true): ContactAddDialog {
        if (initBean(task, selectAbleRoles, showRoles, addableRoles, addContactAsRole, manuallySelectRole, autoRoleSelection))
            prepareDialog();
        return this;
    }

    override fun initBean(task: Task): Boolean {
        return initBean(task, ContactRole.values(), ContactRole.values())
    }

    fun initBean(task: Task,
                 selectAbleRoles: Array<ContactRole> = ContactRole.values(),
                 showRoles: Array<ContactRole> = ContactRole.values(),
                 addableRoles: Array<ContactRole> = ContactRole.values(),
                 addContactAsRole: ContactRole = ContactRole.NONE,
                 manuallySelectRole: Boolean = false,
                 autoRoleSelection: Boolean = true): Boolean {

        logger.debug("Initializing ContactAddDialog")
        super.initBean(task)

        this.addableRoles = addableRoles
        this.selectAbleRoles = selectAbleRoles
        this.showRoles = showRoles
        this.addContactAsRole = addContactAsRole
        this.manuallySelectRole = manuallySelectRole
        this.autoRoleSelection = autoRoleSelection
        this.selectedContact = null

        update(true)

        return true
    }

    /**
     * Updates the contacts of a task.
     */
    override fun update(reload: Boolean) {
        if (reload) {
            task = taskRepository.findByID(task.id, false, false, false, true, true)
        }

        if (showRoles.isNotEmpty())
            contactList = physicianRepository.findAllByRole(showRoles, true, SortOrder.PRIORITY).mapIndexed { index, p -> ContactPhysicianSelector(p, index.toLong(), task) }
    }

    /**
     * If autoRoleSelection is disabled the addContactRole will be returned. If autoRoleSelection
     * is enabled the addableRoles will be check against the physician roles. The first matching
     * role will be returned.
     */
    private fun getDynamicRoleForContact(physician: Physician): ContactRole {
        if (autoRoleSelection) {
            val first = addableRoles.firstOrNull { p -> physician.associatedRoles.contains(p) }

            return first ?: ContactRole.OTHER_PHYSICIAN
        }

        return addContactAsRole ?: ContactRole.NONE
    }

    /**
     * Returns the selected contact and hides the dialog
     */
    fun selectAndHide() {
        logger.debug("Hiding contactAddDialog width $selectedContact")
        if (selectedContact != null)
            super.hideDialog(PhysicianSelectEvent(selectedContact?.physician
                    ?: Physician(), getDynamicRoleForContact(selectedContact?.physician
                    ?: Physician())))
        else
            super.hideDialog()

    }

    /**
     * Removes a contact form the task
     */
    open fun removeContact(reportIntent: ReportIntent) {
        reportIntentService.removeReportIntent(task, reportIntent)
        update(true)

        // clearing selected contact if it is the given report intent
        if (selectedContact != null && selectedContact?.reportIntentOfTask == reportIntent)
            selectedContact = null

        MessageHandler.sendGrowlMessagesAsResource("growl.contact.removed.headline", "growl.contact.removed.success", reportIntent.person?.getFullName())
    }

    /**
     * Special class for displaying the contacts and marking already selected contacts
     */
    open class ContactPhysicianSelector(physician: Physician, id: Long, task: Task) : PhysicianSelector(physician, id) {
        val reportIntentOfTask = task.contacts.firstOrNull { it.person == physician.person }
        val contactOfTask = reportIntentOfTask != null
        val removeAble: Boolean = (reportIntentOfTask?.deleteable
                ?: true) || (reportIntentOfTask?.let { !SpringContextBridge.services().reportIntentService.isHistoryPresent(it) }
                ?: true)
    }
}