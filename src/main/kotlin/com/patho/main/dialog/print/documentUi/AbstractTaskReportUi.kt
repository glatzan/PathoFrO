package com.patho.main.dialog.print.documentUi

import com.patho.main.common.ContactRole
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.PrintDocument
import com.patho.main.util.ui.selector.ReportIntentSelector

abstract class AbstractTaskReportUi<T : PrintDocument, S : AbstractTaskReportUi.SharedContactData>(printDocument: T, sharedData: S) : AbstractTaskDocumentUi<T, S>(printDocument, sharedData) {
    /**
     * Pointer for printing all selected contacts
     */
    var contactListPointer: ReportIntentSelector? = null

    /**
     * Updates the pdf content if a associatedContact was chosen for the first time
     */
    fun onChooseContact(container: ReportIntentSelector) {
        if (!container.selected)
        // if container was deselected, deselect all organizations
            container.organizations.forEach { p -> p.selected = false }
        else {

            // setting default address to true
            if (container.contact.person?.defaultAddress != null) {
                for (organizationChooser in container.organizations) {
                    if (organizationChooser.organization == container.contact.person!!.defaultAddress) {
                        organizationChooser.selected = true
                        break
                    }
                }
            }

            // if single select mode remove other selections
            if (sharedData.isSingleSelect) {
                sharedData.contactList.forEach { p ->
                    if (p !== container) {
                        p.selected = false
                        p.organizations.forEach { s -> s.selected = false }
                    }
                }
            }
        }

        container.updateAddress()

    }

    /**
     * Updates the person if a organization was selected or deselected
     */
    fun onChooseOrganizationOfContact(chooser: ReportIntentSelector.OrganizationSelector) {
        if (chooser.selected) {
            // only one organization can be selected, removing other
            // organizations
            // from selection
            if (chooser.parent.selected) {
                for (organizationSelector in chooser.parent
                        .organizations) {
                    if (organizationSelector !== chooser) {
                        organizationSelector.selected = false
                    }
                }
            } else {
                // setting parent as selected
                chooser.parent.selected = true
            }

            // if single select mode remove other selections
            if (sharedData.isSingleSelect) {
                sharedData.contactList.forEach { p ->
                    if (p !== chooser.parent) {
                        p.selected = false
                        p.organizations.forEach { s -> s.selected = false }
                    }
                }
            }
        }

        chooser.parent.updateAddress(true)
    }

    /**
     * Gets the address of the first selected contact
     */
    fun getAddressOfFirstSelectedContact(): String {
        val c = sharedData.contactList.firstOrNull { p -> p.selected } ?: return ""
        return if (c.emptyAddress == false) c.customAddress ?: "" else ""
    }

    /**
     * Returns the first selected contact
     */
    override fun getFirstSelectedContact(): ReportIntent? {
        return sharedData.contactList.firstOrNull { p -> p.selected }?.contact
    }

    /**
     * Resets the template pointer
     */
    override fun beginNextTemplateIteration() {
        contactListPointer = null
    }

    /**
     * Checks if an other template configuration is present
     */
    override fun hasNextTemplateConfiguration(): Boolean {
        var searchForNextContact = false

        for (contactSelector in sharedData.contactList) {
            if (contactSelector.selected) {
                // first contact pointer
                if (contactListPointer == null) {
                    contactListPointer = contactSelector
                    return true
                } else if (searchForNextContact) {
                    contactListPointer = contactSelector
                    return true
                } else if (contactListPointer === contactSelector) {
                    searchForNextContact = true
                    continue
                }
            }
        }
        return false
    }

    /**
     * Data that can be share between AbstractTaskReportUi Objects.
     */
    open class SharedContactData : AbstractDocumentUi.SharedData() {

        /**
         * Task
         */
        lateinit var task: Task

        /**
         * List with all associated contacts
         */
        var contactList: MutableList<ReportIntentSelector> = mutableListOf()

        /**
         * If true single select mode of contacts is enabled
         */
        var isSingleSelect: Boolean = false

        /**
         * If true the pdf will be updated on every settings change
         */
        var isUpdatePdfOnEverySettingChange = false

        /**
         * If true the first selected contact will be rendered
         */
        var isRenderSelectedContact = false

        /**
         * Initializes the shareddata context.
         */
        open fun initialize(task: Task): Boolean {
            // return if already initialized
            if (super.initialize())
                return true

            this.task = task
            return false
        }

        /**
         * Initializes the shareddata context.
         */
        open fun initialize(task: Task, contactSelector: List<ReportIntentSelector>): Boolean {
            // return if already initialized
            if (super.initialize())
                return true

            this.task = task

            // setting other contacts (physicians)
            this.contactList.addAll(contactSelector)

            // setting custom address
            this.contactList.add(ReportIntentSelector(task,
                    Person(SpringContextBridge.services().resourceBundle.get("dialog.printDialog.individualAddress"), Contact()),
                    ContactRole.NONE))

            // setting blank address
            this.contactList.add(ReportIntentSelector(task,
                    Person(SpringContextBridge.services().resourceBundle.get("dialog.print.blankAddress"), Contact()), ContactRole.NONE,
                    true, true))

            return false
        }
    }
}
