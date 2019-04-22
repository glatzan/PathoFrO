package com.patho.main.util.ui.selector

import com.patho.main.model.patient.Task
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.person.Organization
import com.patho.main.service.impl.SpringContextBridge

/**
 * Selector for reportinetnts an their organizations. Is used in the print dialog
 */
class ReportIntentSelector(reportIntent: ReportIntent, id: Long = reportIntent.id, selected: Boolean = false, emptyAddress: Boolean = false) : UISelector<ReportIntent>(reportIntent, id) {

    /**
     * Returns the item form the contect field
     */
    val contact
        get() = item

    /**
     * Number of copies to print
     */
    var copies: Int = 1

    /**
     * List of organization selectors
     */
    var organizations: MutableList<OrganizationSelector> = mutableListOf()

    /**
     * True if address should be empty
     */
    var emptyAddress: Boolean = emptyAddress

    /**
     * Save field for the address
     */
    var customAddress: String = if (!emptyAddress) generateAddress() else ""

    /**
     * True if address was manually altered
     */
    var manuallyAltered: Boolean = false

    /**
     * Creates organization selectors
     */
    init {
        this.selected = selected
        reportIntent.person?.organizsations?.forEach {
            organizations.add(OrganizationSelector(this, it, it == reportIntent.person?.defaultAddress))
        }


    }

    /**
     * Returns the first selected organization
     */
    fun getSelectedOrganization(): OrganizationSelector? {
        return organizations.firstOrNull { it.selected }
    }

    /**
     * Generates the address base on the contact and organization selection
     */
    fun generateAddress(): String {
        val selectedOrganization = getSelectedOrganization()?.organization

        val customAddress = SpringContextBridge.services().reportIntentService.generateAddress(contact, selectedOrganization)

        logger.trace("Custom Address is: $customAddress")
        return customAddress
    }

    /**
     * Organization sub selector
     */
    class OrganizationSelector(val parent: ReportIntentSelector, val organization: Organization, selected: Boolean = false) {
        var selected: Boolean = selected
    }

    companion object {
        /**
         * Factory method for a task
         */
        @JvmStatic
        fun factory(task: Task): List<ReportIntentSelector> {
            return task.contacts.map { p -> ReportIntentSelector(p) }
        }
    }

}