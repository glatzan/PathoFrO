package com.patho.main.util.notification;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.service.AssociatedContactService;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.pdf.PDFGenerator;

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

	private PathoConfig pathoConfig;

	private Task task;
	private DiagnosisRevision diagnosisRevision;

	private PDFContainer genericReport;
	private PrintDocument genericTemplate;

	private boolean useMail;
	private boolean individualMailAddress;
	private PrintDocument mailTemplate;
	private PDFContainer mailGenericReport;
	private List<NotificationContainer> mails;

	private boolean useFax;
	private boolean individualFaxAddress;
	private PrintDocument faxTemplate;
	private PDFContainer faxGenericReport;
	private List<NotificationContainer> faxes;

	private boolean useLetter;
	private boolean individualLetterAddress;
	private PrintDocument letterTemplate;
	private PDFContainer letterGenericReport;
	private List<NotificationContainer> letters;

	private boolean usePhone;
	private List<NotificationContainer> phonenumbers;

	public NotificationPerformer(Task task, DiagnosisRevision diagnosisRevision) {
		this.task = task;
		this.diagnosisRevision = diagnosisRevision;
	}

	public void mailNotification(List<NotificationContainer> mails, boolean individualMailAddress,
			PrintDocument mailTemplate) {
		this.useMail = mails != null && !mails.isEmpty();
		this.useFax = faxes != null && !faxes.isEmpty();
		this.individualLetterAddress = individualMailAddress;
		this.mailTemplate = mailTemplate;

		if (useMail) {
			if (!individualMailAddress && mailTemplate == genericTemplate) {
				mailGenericReport = genericReport;
			} else {
				mailGenericReport = generatePDF(task, diagnosisRevision, "", mailTemplate);
			}
		}
	}

	public void faxNotification(List<NotificationContainer> faxes) {
		this.useFax = faxes != null && !faxes.isEmpty();
		this.faxes = faxes;

		if (useFax) {
			if (!individualFaxAddress && faxTemplate == genericTemplate) {
				faxGenericReport = genericReport;
			} else {
				faxGenericReport = generatePDF(task, diagnosisRevision, "", faxTemplate);
			}
		}
	}

	public void letterNotification(List<NotificationContainer> letters) {
		this.useLetter = phonenumbers != null && !phonenumbers.isEmpty();
		this.letters = letters;

		if (useLetter) {
			if (!individualFaxAddress && letterTemplate == genericTemplate) {
				letterGenericReport = genericReport;
			} else {
				letterGenericReport = generatePDF(task, diagnosisRevision, "", letterTemplate);
			}
		}
	}

	public void phoneNotification(List<NotificationContainer> phonenumbers) {
		this.usePhone = letters != null && !letters.isEmpty();
		this.phonenumbers = phonenumbers;
	}
	/**
	 * Returns a pdf container for an contact. IF the contact has its own container,
	 * that container is returned. IF individualAddresses is true a new pdf with the
	 * address of the of the container will be generated. Otherwise a generic pdf
	 * will be returned.
	 */
	public PDFContainer getPDFForContainer(NotificationContainer container) {
		if (container.getPdf() != null) {
			// pdf was selected for the individual contact
			// adding pdf to generated pdf array
			return container.getPdf();
		} else {
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
					feedback.setFeedback("dialog.notification.sendProcess.pdf.generating",
							container.getContact().getPerson().getFullName());

					container.setPdf(generatePDF(task, diagnosisRevision, reportAddressField, template));

					logger.debug("Returning individual address");
				}

				return container.getPdf();
			}
		}

		logger.debug("Returning no pdf");

		return null;
	}

	private PDFContainer generatePDF(Task task, DiagnosisRevision diagnosisRevision, String address,
			PrintDocument document) {
		PDFGenerator generator = new PDFGenerator();

		document.initilize(new InitializeToken("task", task),
				new InitializeToken("diagnosisRevisions", Arrays.asList(diagnosisRevision)),
				new InitializeToken("patient", task.getPatient()), new InitializeToken("address", address),
				new InitializeToken("subject", ""));

		PDFContainer container = generator.getPDF(document, new File(pathoConfig.getFileSettings().getPrintDirectory()),
				false);

		return container;
	}
}
