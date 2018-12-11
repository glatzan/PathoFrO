package com.patho.main.action.dialog.print;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.AssociatedContactNotification.NotificationTyp;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.ui.LazyPDFGuiManager;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.pdf.PDFGenerator;
import com.patho.main.util.pdf.PrintOrder;
import com.patho.main.util.printer.TemplatePDFContainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PrintDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PrintDocumentRepository printDocumentRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactService associatedContactService;

	/**
	 * Manager for rendering the pdf lazy style
	 */
	private LazyPDFGuiManager guiManager = new LazyPDFGuiManager();

	/**
	 * List of all templates for printing
	 */
	private List<AbstractDocumentUi<?, ?>> templateList;

	/**
	 * The TemplateListtransformer for selecting a template
	 */
	private DefaultTransformer<AbstractDocumentUi<?, ?>> templateTransformer;

	/**
	 * Ui object for template
	 */
	private AbstractDocumentUi<?, ?> selectedTemplate;

	/**
	 * Can be set to true, if so the generated pdf will be saved
	 */
	private boolean savePDF;

	/**
	 * if true no print button, but instead a select button will be display
	 */
	private boolean selectMode;

	/**
	 * If True the template will be returned as well
	 */
	private boolean selectWithTemplate;

	/**
	 * If true only on address can be selected
	 */
	private boolean singleAddressSelectMode;

	/**
	 * If true a fax button will be displayed
	 */
	private boolean faxMode;

	/**
	 * if true duplex printing will be used
	 */
	private boolean duplexPrinting;

	/**
	 * Only in use if duplexPrinting is true. IF printEvenPageCounts is true a blank
	 * page will be added if there is an odd number of pages to print.
	 */
	private boolean printEvenPageCounts;

	/**
	 * Initializes the bean and shows the print dialog
	 * 
	 * @param task
	 */
	public PrintDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	/**
	 * Initializes the bean and shows the print dialog
	 * 
	 * @param task
	 */
	public PrintDialog initAndPrepareBean(Task task, List<AbstractDocumentUi<?, ?>> templateUI,
			AbstractDocumentUi<?, ?> selectedTemplateUi) {
		if (initBean(task, templateUI, selectedTemplateUi))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task) {
		List<PrintDocument> printDocuments = printDocumentRepository.findAllByTypes(DocumentType.DIAGNOSIS_REPORT,
				DocumentType.U_REPORT, DocumentType.U_REPORT_EMTY, DocumentType.DIAGNOSIS_REPORT_EXTERN);

		// getting ui objects
		List<AbstractDocumentUi<?, ?>> printDocumentUIs = AbstractDocumentUi.factory(printDocuments);

		// init templates
		printDocumentUIs.forEach(p -> p.initialize(task));

		return initBean(task, printDocumentUIs, printDocumentUIs.get(0));
	}

	public boolean initBean(Task task, List<AbstractDocumentUi<?, ?>> templateUI,
			AbstractDocumentUi<?, ?> selectedTemplateUi) {

		if (templateUI != null) {

			// setting template list to choose from
			setTemplateList(templateUI);
			setTemplateTransformer(new DefaultTransformer<AbstractDocumentUi<?, ?>>(getTemplateList()));

			if (selectedTemplateUi != null)
				setSelectedTemplate(selectedTemplateUi);
			else
				setSelectedTemplate(templateUI.get(0));

			guiManager.setRenderComponent(true);
		} else {
			guiManager.setRenderComponent(false);
		}

		guiManager.reset();

		setSelectMode(false);
		setFaxMode(true);
		setSavePDF(true);
		setSingleAddressSelectMode(false);

		super.initBean(task, Dialog.PRINT);

		// rendering the template
		onChangePrintTemplate();

		return true;
	}

	public PrintDialog printMode() {
		return this;
	}

	public PrintDialog selectMode() {
		return selectMode(false);
	}

	public PrintDialog selectMode(boolean selectWithTemplate) {
		setSelectMode(true);
		return this;
	}

	public void onChangePrintTemplate() {
		guiManager.reset();
		guiManager.startRendering(getSelectedTemplate().getDefaultTemplateConfiguration().getDocumentTemplate(),
				pathoConfig.getFileSettings().getPrintDirectory());

		setDuplexPrinting(getSelectedTemplate().getPrintDocument().isDuplexPrinting());
		setPrintEvenPageCounts(getSelectedTemplate().getPrintDocument().isPrintEvenPageCount());
	}

	public void onPrintNewPdf() {

		logger.debug("Printing PDF");

		PDFGenerator generator = new PDFGenerator();

		getSelectedTemplate().beginNextTemplateIteration();

		int printedDocuments = 0;

		while (getSelectedTemplate().hasNextTemplateConfiguration()) {
			AbstractDocumentUi<?, ?>.TemplateConfiguration<?> container = getSelectedTemplate()
					.getNextTemplateConfiguration();

			PDFContainer pdf = generator.getPDF(container.getDocumentTemplate(),
					new File(pathoConfig.getFileSettings().getPrintDirectory()));

			PrintOrder printOrder = new PrintOrder(pdf, container.getCopies(), isDuplexPrinting(),
					container.getDocumentTemplate().getAttributes());

			userHandlerAction.getSelectedPrinter().print(printOrder);

			// only save if person is associated
			if (container.getContact() != null && container.getContact().getRole() != ContactRole.NONE) {
				associatedContactService.addNotificationByType(container.getContact(), NotificationTyp.PRINT, false,
						true, false, new Date(System.currentTimeMillis()), container.getAddress(), false);
			}

			printedDocuments += container.getCopies();
			logger.debug("Printing next order ");
		}

		MessageHandler.sendGrowlMessagesAsResource("growl.print", "growl.print.success",
				new Object[] { printedDocuments });

		logger.debug("Printing completed");
	}

	/**
	 * Saves a new pdf within the task
	 * 
	 * @param pdf
	 */
	public void savePdf(Task task, PDFContainer pdf) {

		// if (pdf.getId() == 0) {
		// logger.debug("Pdf not saved jet, saving" + pdf.getName());
		//
		// // saving new pdf and updating task
		// pdfRepository.save(pdf, resourceBundle.get("log.pdf.created",
		// pdf.getName()));
		//
		// task.getAttachedPdfs().add(pdf);
		//
		// taskRepository.save(task, resourceBundle.get("log.pdf.attached",
		// pdf.getName()));
		//
		// } else {
		// logger.debug("PDF allready saved, not saving. " + pdf.getName());
		// }
	}

	/**
	 * Returns the rendered pdf if in selectMode
	 */
	public void hideAndSelectDialog() {
		super.hideDialog(selectWithTemplate
				? new TemplatePDFContainer(guiManager.getPDFContainerToRender(),
						getSelectedTemplate().getDefaultTemplateConfiguration().getDocumentTemplate())
				: guiManager.getPDFContainerToRender());
	}
}
