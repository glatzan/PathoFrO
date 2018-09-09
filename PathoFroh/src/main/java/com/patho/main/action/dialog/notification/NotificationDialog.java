package com.patho.main.action.dialog.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.DialogHandlerAction;
import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.AssociatedContactNotification.NotificationTyp;
import com.patho.main.model.Contact;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Person;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.service.NotificationService;
import com.patho.main.service.PDFService;
import com.patho.main.service.TaskService;

import com.patho.main.template.MailTemplate;
import com.patho.main.template.mail.DiagnosisReportMail;
import com.patho.main.template.print.DiagnosisReport;
import com.patho.main.template.print.ui.AbstractDocumentUi;
import com.patho.main.template.print.ui.DiagnosisReportUi;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.impl.DefaultDataList;
import com.patho.main.util.notification.FaxExecutor;
import com.patho.main.util.notification.MailContainerList;
import com.patho.main.util.notification.MailExecutor;
import com.patho.main.util.notification.NotificationContainer;
import com.patho.main.util.notification.NotificationContainerList;
import com.patho.main.util.notification.NotificationFeedback;
import com.patho.main.util.pdf.PDFGenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Getter
@Setter
@Slf4j
public class NotificationDialog extends AbstractTabDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DialogHandlerAction dialogHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private GlobalSettings globalSettings;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TransactionTemplate transactionTemplate;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserHandlerAction userHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private NotificationService notificationService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskService taskService;

	private GeneralTab generalTab;
	private MailTab mailTab;
	private FaxTab faxTab;
	private LetterTab letterTab;
	private PhoneTab phoneTab;
	private SendTab sendTab;
	private SendReportTab sendReportTab;

	public void initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
	}

	public boolean initBean(Task task) {
		return initBean(task, false);
	}

	public boolean initBean(Task task, boolean resend) {
	
		setGeneralTab(new GeneralTab());
		setMailTab(new MailTab());
		setFaxTab(new FaxTab());
		setLetterTab(new LetterTab());
		setPhoneTab(new PhoneTab());
		setSendTab(new SendTab());
		setSendReportTab(new SendReportTab());

		tabs = new AbstractTab[] { generalTab, mailTab, faxTab, letterTab, phoneTab, sendTab, sendReportTab };

		super.initBean(task, Dialog.NOTIFICATION);

		// disabling tabs if notification was performed
		if (task.getNotificationCompletionDate() != 0 && !resend) {
			generalTab.setDisabled(true);
			mailTab.setDisabled(true);
			faxTab.setDisabled(true);
			letterTab.setDisabled(true);
			phoneTab.setDisabled(true);
			sendTab.setDisabled(true);
			sendReportTab.setDisabled(false);

			onTabChange(sendReportTab);
		} else if (resend) {
			generalTab.setDisabled(false);
			mailTab.setDisabled(false);
			faxTab.setDisabled(generalTab.isTemporaryNotification());
			letterTab.setDisabled(generalTab.isTemporaryNotification());
			phoneTab.setDisabled(generalTab.isTemporaryNotification());
			sendTab.setDisabled(false);
			sendReportTab.setDisabled(false);
		} else {
			generalTab.setDisabled(false);
			mailTab.setDisabled(false);
			faxTab.setDisabled(generalTab.isTemporaryNotification());
			letterTab.setDisabled(generalTab.isTemporaryNotification());
			phoneTab.setDisabled(generalTab.isTemporaryNotification());
			sendTab.setDisabled(false);
			sendReportTab.setDisabled(true);
		}
		return true;
	}

	public void openSelectPDFDialog(Task task, AssociatedContact contact) {

		List<DocumentTemplate> templates = DocumentTemplate.getTemplates(DocumentType.DIAGNOSIS_REPORT,
				DocumentType.DIAGNOSIS_REPORT_EXTERN);
		List<AbstractDocumentUi<?>> subSelectUIs = templates.stream().map(p -> p.getDocumentUi())
				.collect(Collectors.toList());

		ArrayList<ContactSelector> selectors = new ArrayList<ContactSelector>();

		selectors.add(new ContactSelector(contact));
		selectors.add(new ContactSelector(task,
				new Person(resourceBundle.get("dialog.print.individualAddress"), new Contact()), ContactRole.NONE));

		for (AbstractDocumentUi<?> documentUi : subSelectUIs) {
			((DiagnosisReportUi) documentUi).initialize(task, selectors);
			((DiagnosisReportUi) documentUi).setSingleSelect(true);
			((DiagnosisReportUi) documentUi)
					.setSelectedDiagnosis(task.getDiagnosisRevisions().get(task.getDiagnosisRevisions().size() - 1));
		}

		selectors.get(0).setSelected(true);

		dialogHandlerAction.getPrintDialog().initBeanForSelecting(task, subSelectUIs, DocumentType.DIAGNOSIS_REPORT);
		dialogHandlerAction.getPrintDialog().setFaxMode(false);
		dialogHandlerAction.getPrintDialog().prepareDialog();
	}

	/**
	 * Adds a pdf to the selected container
	 * 
	 * @param event
	 */
	public void onSelectPDFDialogReturn(SelectEvent event) {
		Object container = event.getComponent().getAttributes().get("container");

		log.debug("On custom dialog return " + event.getObject() + " " + container);

		if (event.getObject() != null && event.getObject() instanceof PDFContainer && container != null
				&& container instanceof NotificationContainer) {
			log.debug("Settign custom pdf for container "
					+ ((NotificationContainer) container).getContact().getPerson().getFirstName());
			((NotificationContainer) container).setPdf((PDFContainer) event.getObject());
		}

	}

	public void openMediaViewDialog(PDFContainer container) {

		DefaultDataList dataList = new DefaultDataList(container);

		// init dialog for patient and task
		dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(), new DataList[] { dataList }, dataList,
				container, false, false);

		// setting info text
		dialogHandlerAction.getMediaDialog()
				.setActionDescription(resourceBundle.get("dialog.pdfOrganizer.headline.info.council", getTask().getTaskID()));

		// show dialog
		dialogHandlerAction.getMediaDialog().prepareDialog();
	}

	@Getter
	@Setter
	public abstract class NotificationTab extends AbstractTab {

		protected NotificationContainerList containerList;

		protected boolean initialized;

		protected List<DocumentTemplate> templateList;

		protected DefaultTransformer<DocumentTemplate> templateTransformer;

		/**
		 * Updates the notification container list and if at least one notification for
		 * this task should be performed useTab is set to true
		 */
		@Override
		public void updateData() {
			log.debug("Updating tab " + containerList.getNotificationTyp());
			containerList.updateList(task.getContacts(), containerList.getNotificationTyp());
			containerList.setUse(containerList.getContainer().size() > 0);
			setInitialized(true);
		}
	}

	@Getter
	@Setter
	public class GeneralTab extends NotificationTab {

		private List<DiagnosisRevision> diagnoses;

		private DefaultTransformer<DiagnosisRevision> diagnosesTransformer;

		private DiagnosisRevision selectedDiagnosis;

		private boolean temporaryNotification;

		public GeneralTab() {
			setTabName("GeneralTab");
			setName("dialog.notification.tab.general");
			setViewID("generalTab");
			setCenterInclude("include/general.xhtml");
		}

		@Override
		public boolean initTab() {
			setContainerList(new NotificationContainerList(NotificationTyp.PRINT));

			setTemplateList(
					DocumentTemplate.getTemplates(DocumentType.DIAGNOSIS_REPORT, DocumentType.DIAGNOSIS_REPORT_EXTERN));

			setTemplateTransformer(new DefaultTransformer<DocumentTemplate>(getTemplateList()));

			getContainerList().setDefaultReport((DiagnosisReport) DocumentTemplate.getTemplateByID(getTemplateList(),
					globalSettings.getDefaultDocuments().getNotificationDefaultPrintDocument()));

			getContainerList().setPrintCount(2);

			setDiagnoses(task.getDiagnosisRevisions());
			setDiagnosesTransformer(new DefaultTransformer<DiagnosisRevision>(getDiagnoses()));

			for (DiagnosisRevision revision : task.getDiagnosisRevisions()) {
				if (revision.isNotificationPending()) {
					setSelectedDiagnosis(revision);
					// last element
					if (task.getDiagnosisRevisions().indexOf(revision) == task.getDiagnosisRevisions().size() - 1)
						setTemporaryNotification(false);
					else
						setTemporaryNotification(true);
				}
			}

			// if no diagnosis is pending setting last diagnosis
			if (getSelectedDiagnosis() == null) {
				setSelectedDiagnosis(task.getDiagnosisRevisions().get(task.getDiagnosisRevisions().size() - 1));
				setTemporaryNotification(false);
			}

			getContainerList().setUse(true);

			log.debug("General Data initialized");
			return true;
		}

		public void updateData() {
		}

	}

	@Getter
	@Setter
	public class MailTab extends NotificationTab {

		private String mailSubject;

		private String mailBody;

		public MailTab() {
			setTabName("MailTab");
			setName("dialog.notification.tab.mail");
			setViewID("mailTab");
			setCenterInclude("include/mail.xhtml");
		}

		@Override
		public boolean initTab() {
			setContainerList(new MailContainerList(NotificationTyp.EMAIL));

			getContainerList().setSend(true);

			setTemplateList(
					DocumentTemplate.getTemplates(DocumentType.DIAGNOSIS_REPORT, DocumentType.DIAGNOSIS_REPORT_EXTERN));

			setTemplateTransformer(new DefaultTransformer<DocumentTemplate>(getTemplateList()));

			getContainerList().setDefaultReport((DiagnosisReport) DocumentTemplate.getTemplateByID(getTemplateList(),
					globalSettings.getDefaultDocuments().getNotificationDefaultEmailDocument()));

			DiagnosisReportMail mail = MailTemplate
					.getTemplateByID(globalSettings.getDefaultDocuments().getNotificationDefaultEmail());

			mail.prepareTemplate(task.getPatient(), task, null);
			mail.fillTemplate();

			((MailContainerList) getContainerList()).setSelectedMail(mail);

			setMailSubject(mail.getSubject());
			setMailBody(mail.getBody());

			log.debug("Mail data initialized");
			return true;
		}
	}

	@Getter
	@Setter
	public class FaxTab extends NotificationTab {

		public FaxTab() {
			setTabName("FaxTab");
			setName("dialog.notification.tab.fax");
			setViewID("faxTab");
			setCenterInclude("include/fax.xhtml");
		}

		@Override
		public boolean initTab() {
			setContainerList(new NotificationContainerList(NotificationTyp.FAX));

			getContainerList().setIndividualAddresses(true);
			
			setTemplateList(
					DocumentTemplate.getTemplates(DocumentType.DIAGNOSIS_REPORT, DocumentType.DIAGNOSIS_REPORT_EXTERN));

			setTemplateTransformer(new DefaultTransformer<DocumentTemplate>(getTemplateList()));

			getContainerList().setDefaultReport((DiagnosisReport) DocumentTemplate.getTemplateByID(getTemplateList(),
					globalSettings.getDefaultDocuments().getNotificationDefaultFaxDocument()));

			getContainerList().setSend(true);

			getContainerList().setPrint(false);

			log.debug("Fax data initialized");
			return true;
		}
	}

	@Getter
	@Setter
	public class LetterTab extends NotificationTab {

		public LetterTab() {
			setTabName("LetterTab");
			setName("dialog.notification.tab.letter");
			setViewID("letterTab");
			setCenterInclude("include/letter.xhtml");
		}

		@Override
		public boolean initTab() {
			setContainerList(new NotificationContainerList(NotificationTyp.LETTER));

			getContainerList().setIndividualAddresses(true);
			
			setTemplateList(
					DocumentTemplate.getTemplates(DocumentType.DIAGNOSIS_REPORT, DocumentType.DIAGNOSIS_REPORT_EXTERN));

			setTemplateTransformer(new DefaultTransformer<DocumentTemplate>(getTemplateList()));

			getContainerList().setDefaultReport((DiagnosisReport) DocumentTemplate.getTemplateByID(getTemplateList(),
					globalSettings.getDefaultDocuments().getNotificationDefaultLetterDocument()));

			getContainerList().setPrint(true);

			log.debug("Letter data initialized");
			return true;
		}
	}

	@Getter
	@Setter
	public class PhoneTab extends NotificationTab {

		public PhoneTab() {
			setTabName("PhoneTab");
			setName("dialog.notification.tab.phone");
			setViewID("phoneTab");
			setCenterInclude("include/phone.xhtml");
		}

		@Override
		public boolean initTab() {
			setContainerList(new NotificationContainerList(NotificationTyp.PHONE));

			log.debug("Phone data initialized");
			return true;
		}
	}

	@Getter
	@Setter
	public class SendTab extends NotificationTab implements NotificationFeedback {

		private boolean notificationCompleted;

		private boolean notificationRunning;

		private int progressPercent;

		private String feedbackText;

		private int steps;

		public SendTab() {
			setTabName("SendTab");
			setName("dialog.notification.tab.send");
			setViewID("sendTab");
			setCenterInclude("include/send.xhtml");
		}

		@Override
		public boolean initTab() {
			// clearing all holders
			setNotificationCompleted(false);
			setNotificationRunning(false);
			setProgressPercent(0);
			setFeedbackText("");
			setSteps(1);

			setInitialized(true);
			return true;
		}

		@Override
		public void updateData() {
			log.debug("Update Data send");

			// mail
			if (!mailTab.isInitialized())
				mailTab.updateData();

			MailExecutor mailExecutor = new MailExecutor(null);
			mailTab.getContainerList().renewNotifications(getTask());
			mailTab.getContainerList().getContainerToNotify().forEach(p -> {
				if (!mailExecutor.isAddressApproved(p.getContactAddress()))
					p.setWarning(true, "dialog.notification.sendProcess.mail.error.mailNotValid");
				else
					p.clearWarning();
			});

			// fax
			if (!faxTab.isInitialized())
				faxTab.updateData();

			FaxExecutor faxExecutor = new FaxExecutor(null);
			faxTab.getContainerList().renewNotifications(getTask());
			faxTab.getContainerList().getContainerToNotify().forEach(p -> {
				if (!faxExecutor.isAddressApproved(p.getContactAddress()))
					p.setWarning(true, "dialog.notification.sendProcess.fax.error.numberNotValid");
				else
					p.clearWarning();
			});

			// letters
			if (!letterTab.isInitialized())
				letterTab.updateData();

			letterTab.getContainerList().renewNotifications(getTask());

			// phone
			if (!phoneTab.isInitialized())
				phoneTab.updateData();

			phoneTab.getContainerList().renewNotifications(getTask());
		}

		// @Async("taskExecutor")
		public void performeNotification() {

			log.debug("Startin notification thread");

			try {
				if (isNotificationRunning()) {
					log.debug("Thread allready running, abort new request!");
					return;
				}

				setNotificationRunning(true);

				setSteps(calculateSteps());

				// progressStepText("dialog.notification.sendProcess.starting");

				// copy mail text
				((MailContainerList) mailTab.getContainerList()).getSelectedMail().setSubject(mailTab.getMailSubject());
				((MailContainerList) mailTab.getContainerList()).getSelectedMail().setBody(mailTab.getMailBody());

				// copy selected diagnoses
				List<DiagnosisRevision> revisionsToRender = Arrays.asList(generalTab.getSelectedDiagnosis());

				generalTab.getContainerList().setSelectedRevisions(revisionsToRender);
				mailTab.getContainerList().setSelectedRevisions(revisionsToRender);
				faxTab.getContainerList().setSelectedRevisions(revisionsToRender);
				letterTab.getContainerList().setSelectedRevisions(revisionsToRender);

				notificationService.executeNotification(this, getTask(), (MailContainerList) mailTab.getContainerList(),
						faxTab.getContainerList(), letterTab.getContainerList(), phoneTab.getContainerList(),
						generalTab.getContainerList(), generalTab.isTemporaryNotification());

				setProgressPercent(100);

				// progressStepText("dialog.notification.sendProcess.success");

				log.debug("Messaging ended");

			} catch (Exception e) {
				e.printStackTrace();
			}

			setNotificationCompleted(true);

			setNotificationRunning(false);

			// updating data, loading sendreports
			generalTab.setDisabled(true);
			mailTab.setDisabled(true);
			faxTab.setDisabled(true);
			letterTab.setDisabled(true);
			phoneTab.setDisabled(true);
			sendTab.setDisabled(true);
			sendReportTab.setDisabled(false);

			// unblocking gui and updating content
			PrimeFaces.current().executeScript("onNotificationCompleted();");
		}

		public int calculateSteps() {
			// one step for creating the send report
			int steps = 1;

			steps += mailTab.getContainerList().isUse()
					? mailTab.getContainerList().getContainer().stream().filter(p -> p.isPerform()).count()
					: 0;
			steps += faxTab.getContainerList().isUse()
					? faxTab.getContainerList().getContainer().stream().filter(p -> p.isPerform()).count()
					: 0;
			steps += letterTab.getContainerList().isUse()
					? letterTab.getContainerList().getContainer().stream().filter(p -> p.isPerform()).count()
					: 0;
			steps += phoneTab.getContainerList().isUse() ? 1 : 0;

			return steps;
		}

		/**
		 * Progresses one step
		 */
		public void progressStep() {
			setSteps(getSteps() + 1);
			setProgressPercent(getProgressPercent() + (100 / getSteps()));
		}

		@Synchronized
		public int getProgressPercent() {
			return progressPercent;
		}

		@Synchronized
		public void setProgressPercent(int progressPercent) {
			this.progressPercent = progressPercent;
		}

		@Synchronized
		public boolean isNotificationRunning() {
			return notificationRunning;
		}

		@Synchronized
		public void setNotificationRunning(boolean notificationRunning) {
			this.notificationRunning = notificationRunning;
		}

		@Synchronized
		public boolean isNotificationCompleted() {
			return notificationCompleted;
		}

		@Synchronized
		public void setNotificationCompleted(boolean notificationCompleted) {
			this.notificationCompleted = notificationCompleted;
		}

		@Synchronized
		public void setFeedback(String resKey, String... string) {
			setFeedbackText(resourceBundle.get(resKey, string));
		}

		@Synchronized
		public String getFeedback() {
			return getFeedbackText();
		}

	}

	@Getter
	@Setter
	public class SendReportTab extends NotificationTab {

		private DefaultTransformer<PDFContainer> sendReportConverter;

		private boolean sendReportAvailable;

		public SendReportTab() {
			setTabName("SendReport");
			setName("dialog.notification.tab.sendReport");
			setViewID("sendReportTab");
			setCenterInclude("include/sendReport.xhtml");
		}

		@Override
		public void updateData() {
			log.debug("Update Data sendReport");
			// getting all sendreports
			List<PDFContainer> lists = PDFGenerator.getPDFsofType(task.getAttachedPdfs(),
					DocumentType.MEDICAL_FINDINGS_SEND_REPORT_COMPLETED);

			if (lists.size() > 0) {
				setSendReportAvailable(true);
				// updating mediadialog
				dialogHandlerAction.getMediaDialog().initiBeanForExternalView(lists,
						PDFGenerator.getLatestPDFofType(lists));

				setSendReportConverter(new DefaultTransformer<>(lists));
			} else {
				setSendReportAvailable(false);
			}

		}

		public void repeatNotification() {
			initBean(getTask(), true);
			onTabChange(generalTab);
		}

		public void onReturnDialog(SelectEvent event) {
			log.debug("Dialog return");
			if (event.getObject() != null && event.getObject() instanceof Boolean
					&& ((Boolean) event.getObject()).booleanValue())
				hideDialog();
		}
	}

}
