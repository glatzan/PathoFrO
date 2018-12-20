package com.patho.main.util.notification;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.MailTemplate;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.pdf.PDFCreator;
import com.patho.main.util.printer.TemplatePDFContainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Class containg all notification data in order to perform a notification
 * 
 * @author dvk-glatza
 *
 */
@Getter
@Setter
@Configurable
public class NotificationPerformer {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	private Task task;
	private DiagnosisRevision diagnosisRevision;
	private boolean reperformNotification;

	private boolean printDocument;
	private int printCount;
	private TemplatePDFContainer genericReport;
	private PrintDocument genericTemplate;

	private boolean useMail;
	private boolean individualMailAddress;
	private MailTemplate mail;
	private PrintDocument mailTemplate;
	private TemplatePDFContainer mailGenericReport;
	private List<NotificationContainer> mails;

	private boolean useFax;
	private boolean individualFaxAddress;
	private boolean sendFax;
	private boolean printFax;
	private PrintDocument faxTemplate;
	private TemplatePDFContainer faxGenericReport;
	private List<NotificationContainer> faxes;

	private boolean useLetter;
	private boolean individualLetterAddress;
	private PrintDocument letterTemplate;
	private TemplatePDFContainer letterGenericReport;
	private List<NotificationContainer> letters;

	private boolean usePhone;
	private List<NotificationContainer> phonenumbers;

	public NotificationPerformer(Task task, DiagnosisRevision diagnosisRevision) {
		this.task = task;
		this.diagnosisRevision = diagnosisRevision;
	}

	public void printNotification(boolean printDocument, PrintDocument genericTemplate, int count) {
		this.genericTemplate = genericTemplate;
		this.printDocument = printDocument;
		this.printCount = count;
	}

	public void mailNotification(boolean useMail, List<NotificationContainer> mails, boolean individualMailAddress,
			PrintDocument mailTemplate, MailTemplate mail) {
		this.useMail = useMail;
		this.individualMailAddress = individualMailAddress;
		this.mailTemplate = mailTemplate;
		this.mails = mails;
		this.mail = mail;

		if (useMail) {
			if (!individualMailAddress && mailTemplate == genericTemplate) {
				mailGenericReport = genericReport;
			} else {
				mailGenericReport = generatePDF(task, diagnosisRevision, "", mailTemplate);
			}
		}
	}

	public void faxNotification(boolean useFax, List<NotificationContainer> faxes, boolean individualFaxAddress,
			boolean sendFax, boolean printFax, PrintDocument faxTemplate) {
		this.useFax = useFax;
		this.faxes = faxes;
		this.faxTemplate = faxTemplate;

		if (useFax) {
			if (!individualFaxAddress && faxTemplate == genericTemplate) {
				faxGenericReport = genericReport;
			} else {
				faxGenericReport = generatePDF(task, diagnosisRevision, "", faxTemplate);
			}
		}

		this.sendFax = sendFax;
		this.printFax = printFax;
		this.individualFaxAddress = individualFaxAddress;
	}

	public void letterNotification(boolean useLetter, List<NotificationContainer> letters,
			PrintDocument letterTemplate) {
		this.useLetter = useLetter;
		this.letters = letters;
		this.letterTemplate = letterTemplate;

		if (useLetter) {
			if (!individualFaxAddress && letterTemplate == genericTemplate) {
				letterGenericReport = genericReport;
			} else {
				letterGenericReport = generatePDF(task, diagnosisRevision, "", letterTemplate);
			}
		}
	}

	public void phoneNotification(boolean usePhone, List<NotificationContainer> phonenumbers) {
		this.usePhone = usePhone;
		this.phonenumbers = phonenumbers;
	}

	/**
	 * Returns a pdf container for an contact. IF the contact has its own container,
	 * that container is returned. IF individualAddresses is true a new pdf with the
	 * address of the of the container will be generated. Otherwise a generic pdf
	 * will be returned.
	 */
	public TemplatePDFContainer getPDFForContainer(NotificationContainer container, PrintDocument template,
			boolean individualAddresses, TemplatePDFContainer genericPDF) {

		// pdf was selected for the individual contact
		// adding pdf to generated pdf array
		if (container.getPdf() == null) {
			if (template != null) {
				if (!individualAddresses) {
					// setting generic pdf
					container.setPdf(genericPDF);
					logger.debug("Returning generic pdf " + genericPDF);
				} else {
					// individual address
					String reportAddressField = AssociatedContactService.generateAddress(container.getContact(),
							container.getContact().getPerson().getDefaultAddress());

					logger.debug("Generating pdf for " + reportAddressField);

					container.setPdf(generatePDF(task, diagnosisRevision, reportAddressField, template));

					logger.debug("Returning individual address");
				}
			} else {
				return null;
			}
		}
		logger.debug("Returning no pdf");

		return container.getPdf() instanceof TemplatePDFContainer ? (TemplatePDFContainer) container.getPdf()
				: new TemplatePDFContainer(container.getPdf());

	}

	private TemplatePDFContainer generatePDF(Task task, DiagnosisRevision diagnosisRevision, String address,
			PrintDocument document) {
		PDFCreator generator = new PDFCreator();

		document.initilize(new InitializeToken("task", task),
				new InitializeToken("diagnosisRevisions", Arrays.asList(diagnosisRevision)),
				new InitializeToken("patient", task.getPatient()), new InitializeToken("address", address),
				new InitializeToken("subject", ""));

		TemplatePDFContainer container;
		try {
			container = new TemplatePDFContainer(
					new PDFCreator().createPDF(document, new File(pathoConfig.getFileSettings().getPrintDirectory())),
					document);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
			// TODO handel error
		}

		return container;
	}

	public TemplatePDFContainer getGenericReport() {
		if (isPrintDocument())
			return genericReport != null ? genericReport
					: (genericReport = generatePDF(task, diagnosisRevision, "", genericTemplate));
		return null;
	}

//	public void finishSendProecess(T container, boolean success, String message) {
//		container.getNotification().setPerformed(true);
//		container.getNotification().setDateOfAction(new Date(System.currentTimeMillis()));
//		container.getNotification().setCommentary(message);
//		// if success = performed, nothing to do = inactive, if failed = active
//		container.getNotification().setActive(!success);
//		// if success = !failed = false
//		container.getNotification().setFailed(!success);
//	}

}
