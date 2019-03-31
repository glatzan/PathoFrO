package com.patho.main.dialog.contact

import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.dialog.task.AbstractTaskDialog
import com.patho.main.model.patient.Task
import com.patho.main.ui.selectors.PhysicianSelector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class ContactAddDialog @Autowired constructor(
) : AbstractTaskDialog() {

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
    open var addContactAsRole = ContactRole.NONE

    /**
     * List contain contacts to select from, used by contacts
     */
    open var contactList = listOf<PhysicianSelector>()

    /**
     * For quickContact selection
     */
    open var selectedContact: PhysicianSelector? = null

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

    override fun initBean(task: Task): Boolean {
        selectAbleRoles = ContactRole.values()
        showRoles = ContactRole.values()
        reportIntents = task.contacts.map { p -> ContactDialog.ReportIntentSelector(p, task) }
        return super.initBean(task, Dialog.CONTACTS)

        super.initBean(task, Dialog.CONTACTS_SELECT)
    }

    open fun _setSelectAbleRoles(roles: Array<ContactRole>): ContactAddDialog {
        this.selectAbleRoles = roles
        return this;
    }

    open fun _setShowRoles(roles: Array<ContactRole>): ContactAddDialog {
        this.showRoles = roles
        return this
    }

    open fun _setAddableRoles(roles: Array<ContactRole>): ContactAddDialog {
        this.addableRoles = roles
        return this
    }

    open fun _setAddContactAsRole(role: ContactRole): ContactAddDialog {
        this.addContactAsRole = role
        return this
    }

    open fun _setContactList(contacts: List<PhysicianSelector>): ContactAddDialog {
        this.contactList = contacts
        return this
    }

    open fun _setManuallySelectRole(manuallySelectRole: Boolean): ContactAddDialog {
        this.manuallySelectRole = manuallySelectRole
        return this
    }

    open fun _setAutoRoleSelection(autoRoleSelection: Boolean): ContactAddDialog {
        this.autoRoleSelection = autoRoleSelection
        return this
    }
}