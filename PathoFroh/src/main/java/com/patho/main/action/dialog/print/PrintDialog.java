package com.patho.main.action.dialog.print;

import static org.assertj.core.api.Assertions.contentOf;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.config.PathoConfig;
import com.patho.main.config.PathoConfig.FileSettings;
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PrintDialog extends AbstractDialog<PrintDialog> {

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
	 * Initializes the bean and shows the council dialog
	 * 
	 * @param task
	 */
	public PrintDialog initAndPrepareBean(Task task) {
		if (initBean(task))
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
		setSelectMode(true);
		return this;
	}

	// public void initBeanForSelecting(Task task, DocumentType[] types,
	// DocumentType defaultType,
	// AssociatedContact[] addresses, boolean allowIndividualAddress) {
	// initBeanForSelecting(task, DocumentTemplate.getTemplates(types), defaultType,
	// Arrays.asList(addresses),
	// allowIndividualAddress);
	// }
	//
	// public void initBeanForSelecting(Task task, List<DocumentTemplate> types,
	// DocumentType defaultType,
	// List<AssociatedContact> addresses, boolean allowIndividualAddress) {
	//
	// List<AbstractDocumentUi<?>> subSelectUIs = types.stream().map(p ->
	// p.getDocumentUi())
	// .collect(Collectors.toList());
	//
	// // init templates
	// subSelectUIs.forEach(p -> p.initialize(task));
	//
	// initBeanForSelecting(task, subSelectUIs, defaultType);
	// }
	//
	// public void initBeanForSelecting(Task task, List<AbstractDocumentUi<?>>
	// subSelectUIs, DocumentType defaultType) {
	//
	// subSelectUIs.forEach(p -> {
	// p.setUpdatePdfOnEverySettingChange(true);
	// p.setRenderSelectedContact(true);
	// });
	//
	// setSelectMode(true);
	// }

	public void onChangePrintTemplate() {
		System.out.println("-------------" + getSelectedTemplate());
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

		while (getSelectedTemplate().hasNextTemplateConfiguration()) {
			AbstractDocumentUi<?, ?>.TemplateConfiguration<?> container = getSelectedTemplate()
					.getNextTemplateConfiguration();

			PDFContainer pdf = generator.getPDF(container.getDocumentTemplate(),
					new File(pathoConfig.getFileSettings().getPrintDirectory()));

			PrintOrder printOrder = new PrintOrder(pdf, container.getCopies(), isDuplexPrinting(),
					container.getDocumentTemplate().getAttributes());

			userHandlerAction.getSelectedPrinter().print(printOrder);

			// only save if person is associated
			if (container.getContact().getRole() != ContactRole.NONE) {
				associatedContactService.addNotificationByType(container.getContact(), NotificationTyp.PRINT, false,
						true, false, new Date(System.currentTimeMillis()), container.getAddress(), false);
			}

			logger.debug("Printing next order ");
		}
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
		super.hideDialog(guiManager.getPDFContainerToRender());
	}
}
