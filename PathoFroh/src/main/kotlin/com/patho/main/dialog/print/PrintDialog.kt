package com.patho.main.dialog.print

import com.patho.main.action.handler.MessageHandler
import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTaskDialog
import com.patho.main.model.PDFContainer
import com.patho.main.model.patient.Task
import com.patho.main.template.print.ui.document.AbstractDocumentUi
import com.patho.main.ui.LazyPDFGuiManager
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.pdf.PDFCreator
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.io.FileNotFoundException

@Component()
@Scope(value = "session")
class PrintDialog : AbstractTaskDialog(Dialog.PRINT) {

    /**
     * Manager for rendering the pdf lazy style
     */
    private val guiManager = LazyPDFGuiManager()

    /**
     * List of all templates for printing
     */
    var templateList: List<AbstractDocumentUi<*, *>> = listOf()
        set(value) {
            field = value
            templateTransformer = DefaultTransformer(value)
        }

    /**
     * The TemplateListtransformer for selecting a template
     */
    var templateTransformer: DefaultTransformer<AbstractDocumentUi<*, *>> = DefaultTransformer(templateList)

    /**
     * Ui object for template
     */
    var selectedTemplate: AbstractDocumentUi<*, *>? = null

    /**
     * Can be set to true, if so the generated pdf will be saved
     */
    var savePDF: Boolean = false

    /**
     * if true no print button, but instead a select button will be display
     */
    var selectMode: Boolean = false

    /**
     * If True the template will be returned as well
     */
    var selectWithTemplate: Boolean = false

    /**
     * If true only on address can be selected
     */
    var singleAddressSelectMode: Boolean = false

    /**
     * If true a fax button will be displayed
     */
    var faxMode: Boolean = false

    /**
     * if true duplex printing will be used
     */
    var duplexPrinting: Boolean = false

    /**
     * Only in use if duplexPrinting is true. IF printEvenPageCounts is true a blank
     * page will be added if there is an odd number of pages to print.
     */
    var printEvenPageCounts: Boolean = false

    fun initAndPrepareBean(task: Task, templateUI: List<AbstractDocumentUi<*, *>>,
                           selectedTemplateUi: AbstractDocumentUi<*, *>): PrintDialog {
        if (initBean(task, templateUI, selectedTemplateUi))
            prepareDialog();
        return this;
    }

    fun initBean(task: Task, templateUI: List<AbstractDocumentUi<*, *>>,
                 selectedTemplateUi: AbstractDocumentUi<*, *>): Boolean {

        if (templateUI != null) {

            // setting template list to choose from
            setTemplateList(templateUI)
            setTemplateTransformer(DefaultTransformer<AbstractDocumentUi<*, *>>(getTemplateList()))

            selectedTemplateUi?.let { setSelectedTemplate(it) } ?: setSelectedTemplate(templateUI[0])

            guiManager.isRenderComponent = true
        } else {
            guiManager.isRenderComponent = false
        }

        guiManager.reset()

        setSelectMode(false)
        setFaxMode(true)
        setSavePDF(true)
        setSingleAddressSelectMode(false)

        super.initBean(task, Dialog.PRINT)

        // rendering the template
        onChangePrintTemplate()

        return true
    }

    fun onChangePrintTemplate() {
        guiManager.reset()
        guiManager.startRendering(getSelectedTemplate().getDefaultTemplateConfiguration().getDocumentTemplate(),
                pathoConfig.fileSettings.printDirectory)

        setDuplexPrinting(getSelectedTemplate().getPrintDocument().isDuplexPrinting())
        setPrintEvenPageCounts(getSelectedTemplate().getPrintDocument().isPrintEvenPageCount())
    }

    fun onPrintNewPdf() {

        logger.debug("Printing PDF")

        getSelectedTemplate().beginNextTemplateIteration()

        var printedDocuments = 0

        while (getSelectedTemplate().hasNextTemplateConfiguration()) {
            val container = getSelectedTemplate()
                    .getNextTemplateConfiguration()

            var pdf: PDFContainer? = null

            try {
                pdf = PDFCreator().createPDF(container!!.getDocumentTemplate())
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                MessageHandler.sendGrowlErrorAsResource("growl.error.critical", "growl.print.failed.creatingPDF")
                continue
            }

            val printOrder = PrintOrder(pdf, container!!.getCopies(), isDuplexPrinting(),
                    container!!.getDocumentTemplate().getAttributes())

            userHandlerAction.getSelectedPrinter().print(printOrder)

            // only save if person is associated
            if (container!!.getContact() != null && container!!.getContact().role != ContactRole.NONE) {
                //				reportIntentService.addNotificationHistoryDataAndReportIntentNotification(task,container.getContact(),NotificationTyp.PRINT, get);
                // TODO save print request

                //				associatedContactService.addNotificationByType(container.getContact(), NotificationTyp.PRINT, false,
                //						true, false, Instant.now(), container.getAddress(), false);
            }

            printedDocuments += container!!.getCopies()
            logger.debug("Printing next order ")
        }

        MessageHandler.sendGrowlMessagesAsResource("growl.print.printing", "growl.print.success",
                arrayOf<Any>(printedDocuments))

        logger.debug("Printing completed")
    }


}