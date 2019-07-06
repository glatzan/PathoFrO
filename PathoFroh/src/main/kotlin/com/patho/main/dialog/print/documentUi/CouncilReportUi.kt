package com.patho.main.dialog.print.documentUi

import com.patho.main.common.ContactRole
import com.patho.main.model.patient.Task
import com.patho.main.model.patient.miscellaneous.Council
import com.patho.main.model.person.Contact
import com.patho.main.model.person.Person
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.template.DocumentToken
import com.patho.main.template.print.CouncilReport
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.dialog.event.CustomAddressSelectEvent
import com.patho.main.util.ui.selector.ReportIntentSelector
import org.primefaces.event.SelectEvent

class CouncilReportUi : AbstractTaskReportUi<CouncilReport, CouncilReportUi.CouncilSharedData> {

    constructor(councilReport: CouncilReport) : this(councilReport, CouncilSharedData())

    constructor(councilReport: CouncilReport, councilSharedData: CouncilSharedData) : super(councilReport, councilSharedData) {
        inputInclude = "include/councilReport.xhtml"
    }

    override fun initialize(task: Task) {
        this.initialize(task, null)
    }

    fun initialize(task: Task, selectedCouncil: Council?) {
        sharedData.initialize(task, selectedCouncil)
        super.initialize(task)
    }

    /**
     * Change Address dialog return event handler
     */
    fun onCustomAddressReturn(event: SelectEvent) {
        logger.debug("Returning from custom address dialog")
        val returnObject = event.`object`

        if (returnObject is CustomAddressSelectEvent) {
            returnObject.obj.manuallyAltered = true
            returnObject.obj.customAddress = returnObject.customAddress
            logger.debug("Custom address set to ${returnObject.customAddress}")
        }
    }

    /**
     * Return default template configuration for printing
     */
    override fun getDefaultTemplateConfiguration(): TemplateConfiguration<CouncilReport> {
        printDocument.initialize(DocumentToken("patient", task.parent), DocumentToken("task", task),
                DocumentToken("council", sharedData.selectedCouncil), DocumentToken("address",
                if (sharedData.isRenderSelectedContact) getAddressOfFirstSelectedContact() else ""))

        return TemplateConfiguration<CouncilReport>(printDocument)
    }

    /**
     * Sets the data for the next print
     */
    override fun getNextTemplateConfiguration(): TemplateConfiguration<CouncilReport>? {
        val address = contactListPointer?.customAddress ?: ""
        printDocument.initialize(DocumentToken("patient", task.parent), DocumentToken("task", task),
                DocumentToken("council", sharedData.selectedCouncil),
                DocumentToken("address", address))

        return TemplateConfiguration<CouncilReport>(printDocument,
                contactListPointer?.contact, address, contactListPointer?.copies
                ?: 1)
    }

    /**
     * Shared context data for councils
     */
    class CouncilSharedData : AbstractTaskReportUi.SharedContactData() {

        /**
         * List of all councils
         */
        var councilList: Set<Council> = setOf()
            set(value) {
                field = value
                councilListTransformer = DefaultTransformer(councilList)
            }

        /**
         * Transformer for council
         */
        var councilListTransformer: DefaultTransformer<Council> = DefaultTransformer(councilList)

        /**
         * Council to print
         */
        var selectedCouncil: Council? = null

        fun initialize(task: Task, selectedCouncil: Council?): Boolean {

            if (super.initialize(task))
                return true

            councilList = task.councils
            this.selectedCouncil = selectedCouncil

            updateContactList()
            return false
        }

        /**
         * Updates the contact list for the selected council
         */
        fun updateContactList() {
            // contacts for printing
            contactList.clear()

            // only one address so set as chosen
            if (selectedCouncil != null && selectedCouncil?.councilPhysician != null) {
                val chooser = ReportIntentSelector(task,
                        selectedCouncil?.councilPhysician?.person ?: Person(), ContactRole.CASE_CONFERENCE)
                chooser.selected = true
                // setting patient
                contactList.add(chooser)
            }

            // setting custom address
            this.contactList.add(ReportIntentSelector(task,
                    Person(SpringContextBridge.services().resourceBundle.get("dialog.printDialog.individualAddress"), Contact()),
                    ContactRole.NONE))

            // setting blank address
            this.contactList.add(ReportIntentSelector(task,
                    Person(SpringContextBridge.services().resourceBundle.get("dialog.print.blankAddress"), Contact()), ContactRole.NONE,
                    true, true))
        }
    }
}