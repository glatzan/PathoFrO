package com.patho.main.service;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.patho.main.config.PathoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.AssociatedContactRepository;
import com.patho.main.repository.DiagnosisRevisionRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.template.MailTemplate;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.notification.NotificationContainer;
import com.patho.main.util.notification.NotificationFeedback;
import com.patho.main.util.pdf.PrintOrder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class NotificationService extends AbstractService {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MailService mailService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private FaxService faxService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiagnosisRevisionRepository diagnosisRevisionRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AssociatedContactRepository associatedContactRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PrintDocumentRepository printDocumentRepository;

	public void startNotificationPhase(Task task) {
//		try {
//
//			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//
//				public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
//
//					task.setNotificationCompletionDate(0);
//
//					genericDAO.savePatientData(task, "log.patient.task.phase.notification.enter");
//
//					if (!task.isListedInFavouriteList(PredefinedFavouriteList.NotificationList)) {
//						favouriteListDAO.addReattachedTaskToList(task, PredefinedFavouriteList.NotificationList);
//					}
//				}
//			});
//		} catch (Exception e) {
//			throw new HistoDatabaseInconsistentVersionException(task);
//		}
	}

	public void endNotificationPhase(Task task) {
//		try {
//
//			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//
//				public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
//
//					task.setNotificationCompletionDate(System.currentTimeMillis());
//
//					genericDAO.savePatientData(task, "log.patient.task.phase.notification.end");
//
//					favouriteListDAO.removeReattachedTaskFromList(task, PredefinedFavouriteList.NotificationList);
//				}
//			});
//		} catch (Exception e) {
//			throw new HistoDatabaseInconsistentVersionException(task);
//		}
	}

//	public boolean executeNotification(NotificationFeedback feedback, Task task, MailContainerList mailContainerList,
//			NotificationContainerList faxContainerList, NotificationContainerList letterContainerList,
//			NotificationContainerList phoneContainerList, NotificationContainerList printContainerList,
//			boolean temporaryNotification) {
//
//		boolean emailSendSuccessful = executeMailNotification(feedback, task, mailContainerList, temporaryNotification);
//		boolean faxSendSuccessful = executeFaxNotification(feedback, task, faxContainerList, temporaryNotification);
//		boolean letterSendSuccessful = executeLetterNotification(feedback, task, letterContainerList,
//				temporaryNotification);
//
//		if (printContainerList.isUse() && printContainerList.getDefaultReport() != null) {
//			// addition templates
//			((DiagnosisReport) printContainerList.getDefaultReport()).initData(task, "");
//			PDFContainer report = (new PDFGenerator())
//					.getPDF(((DiagnosisReport) printContainerList.getDefaultReport()));
//
//			userHandlerAction.getSelectedPrinter().print(report, printContainerList.getPrintCount(),
//					printContainerList.getDefaultReport().getAttributes());
//
//		}
//
//		feedback.progressStep();
//

//	public void finishSendProecess(T container, boolean success, String message) {
//	container.getNotification().setPerformed(true);
//	container.getNotification().setDateOfAction(new Date(System.currentTimeMillis()));
//	container.getNotification().setCommentary(message);
//	// if success = performed, nothing to do = inactive, if failed = active
//	container.getNotification().setActive(!success);
//	// if success = !failed = false
//	container.getNotification().setFailed(!success);
//}


//	public boolean endNotification(NotificationPerformer performer, Task task, DiagnosisRevision diagnosisRevision,
//			List<NotificationContainer> emails, List<NotificationContainer> faxes, List<NotificationContainer> letters,
//			List<NotificationContainer> phones, boolean success) {
//
//		Optional<PrintDocument> document = printDocumentRepository
//				.findByID(pathoConfig.getDefaultDocuments().getNotificationSendReport());
//
//		if (!document.isPresent()) {
//			logger.debug("Printdocument not found");
//			return false;
//		}
//
//		document.get().initilize(new InitializeToken("task", task),
//				new InitializeToken("diagnosisRevisions", Arrays.asList(diagnosisRevision)),
//				new InitializeToken("useMail", !emails.isEmpty()), new InitializeToken("mails", emails),
//				new InitializeToken("useFax", !faxes.isEmpty()), new InitializeToken("faxes", faxes),
//				new InitializeToken("useLetter", !letters.isEmpty()), new InitializeToken("letters", letters),
//				new InitializeToken("usePhone", !phones.isEmpty()), new InitializeToken("phonenumbers", phones),
//				new InitializeToken("reportDate", new Date()));
//
//		try {
//			PDFReturn pdfReturn = pdfService.createAndAttachPDF(task, document.get(), true);
//			performer.setSendReport(pdfReturn.getContainer());
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			performer.setSendReport(null);
//		}
//
//		diagnosisRevision.setNotificationStatus(NotificationStatus.NOTIFICATION_COMPLETED);
//		diagnosisRevision.setNotificationDate(Instant.now());
//
//		diagnosisRevisionRepository.save(diagnosisRevision, "log.patient.task.notification.send", task.getPatient());
//
//		return true;
//	}



//	public PDFContainer generateSendReport(NotificationFeedback feedback, Task task,
//			MailContainerList mailContainerList, NotificationContainerList faxContainerList,
//			NotificationContainerList letterContainerList, NotificationContainerList phoneContaienrList,
//			Date notificationDate, boolean temporarayNotification) {
//
//		feedback.setFeedback("log.notification.pdf.sendReport.generation");
//
//		SendReport sendReport = DocumentTemplate
//				.getTemplateByID(globalSettings.getDefaultDocuments().getNotificationSendReport());
//
//		sendReport.initializeTempalte(task, mailContainerList, faxContainerList, letterContainerList,
//				phoneContaienrList, notificationDate, temporarayNotification);
//
//		PDFContainer container = (new PDFGenerator()).getPDF(sendReport);
//
//		return container;
//	}

}
