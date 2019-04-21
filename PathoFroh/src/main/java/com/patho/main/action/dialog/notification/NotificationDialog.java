package com.patho.main.action.dialog.notification;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractTabDialog;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.Dialog;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.NotificationStatus;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.notification.NotificationTyp;
import com.patho.main.model.patient.notification.ReportIntent;
import com.patho.main.model.person.Contact;
import com.patho.main.model.person.Person;
import com.patho.main.repository.MailRepository;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.service.NotificationService;
import com.patho.main.service.PDFService;
import com.patho.main.service.TaskService;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.MailTemplate;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.template.print.ui.document.report.DiagnosisReportUi;
import com.patho.main.ui.pdf.PDFStreamContainer;
import com.patho.main.ui.selectors.ContactSelector;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.notification.NotificationContainer;
import com.patho.main.util.notification.NotificationFeedback;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configurable
@Getter
@Setter
public class NotificationDialog extends AbstractTabDialog {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PDFService pdfService;

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

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PrintDocumentRepository printDocumentRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private MailRepository mailRepository;

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
    @Lazy
    private DialogHandler dialogHandler;

    private GeneralTab generalTab;
    private MailTab mailTab;
    private FaxTab faxTab;
    private LetterTab letterTab;
    private PhoneTab phoneTab;
    private SendTab sendTab;
    private SendReportTab sendReportTab;

    public NotificationDialog initAndPrepareBean(Task task) {
        if (initBean(task))
            prepareDialog();
        return this;
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

        tabs = new AbstractTabDialog.AbstractTab[]{generalTab, mailTab, faxTab, letterTab, phoneTab, sendTab,
                sendReportTab};

        disableTabs(false, false, false, false, false, false, true);

        super.initBean(task, Dialog.NOTIFICATION);

//		// disabling tabs if notification was performed
//		if (task.getNotificationCompletionDate() != 0 && !resend) {
//			generalTab.setDisabled(true);
//			mailTab.setDisabled(true);
//			faxTab.setDisabled(true);
//			letterTab.setDisabled(true);
//			phoneTab.setDisabled(true);
//			sendTab.setDisabled(true);
//			sendReportTab.setDisabled(false);
//
//			onTabChange(sendReportTab);
//		} else if (resend) {
//			generalTab.setDisabled(false);
//			mailTab.setDisabled(false);
//			faxTab.setDisabled(generalTab.isTemporaryNotification());
//			letterTab.setDisabled(generalTab.isTemporaryNotification());
//			phoneTab.setDisabled(generalTab.isTemporaryNotification());
//			sendTab.setDisabled(false);
//			sendReportTab.setDisabled(false);
//		} else {
//			generalTab.setDisabled(false);
//			mailTab.setDisabled(false);
//			faxTab.setDisabled(generalTab.isTemporaryNotification());
//			letterTab.setDisabled(generalTab.isTemporaryNotification());
//			phoneTab.setDisabled(generalTab.isTemporaryNotification());
//			sendTab.setDisabled(false);
//			sendReportTab.setDisabled(true);
//		}
        return true;
    }

    public void openSelectPDFDialog(Task task, ReportIntent contact) {

        List<PrintDocument> printDocuments = printDocumentRepository.findAllByTypes(DocumentType.DIAGNOSIS_REPORT,
                DocumentType.DIAGNOSIS_REPORT_EXTERN);

        ArrayList<ContactSelector> selectors = new ArrayList<ContactSelector>();

        selectors.add(new ContactSelector(contact));
        selectors.add(new ContactSelector(task,
                new Person(resourceBundle.get("dialog.printDialog.individualAddress"), new Contact()), ContactRole.NONE));
        selectors.get(0).setSelected(true);

        // getting ui objects
        List<AbstractDocumentUi<?, ?>> printDocumentUIs = AbstractDocumentUi.factory(printDocuments);

        for (AbstractDocumentUi<?, ?> documentUi : printDocumentUIs) {
            ((DiagnosisReportUi) documentUi).initialize(task, selectors);
            ((DiagnosisReportUi) documentUi).getSharedData().setRenderSelectedContact(true);
            ((DiagnosisReportUi) documentUi).getSharedData().setUpdatePdfOnEverySettingChange(true);
            ((DiagnosisReportUi) documentUi).getSharedData().setSingleSelect(true);
        }

//        dialogHandler.getPrintDialog().initAndPrepareBean(getTask(), printDocumentUIs, printDocumentUIs.get(0))
//                .selectMode(true);
    }

    /**
     * Adds a pdf to the selected container
     *
     * @param event
     */
    public void onSelectPDFDialogReturn(SelectEvent event) {
        Object container = event.getComponent().getAttributes().get("container");

        logger.debug("On custom dialog return " + event.getObject() + " " + container);

//		if (event.getObject() != null && event.getObject() instanceof PDFContainer && container != null
//				&& container instanceof NotificationContainer) {
//			logger.debug("Settign custom pdf for container "
//					+ ((NotificationContainer) container).getContact().getPerson().getFirstName());
//			((NotificationContainer) container).setPdf((TemplatePDFContainer) event.getObject());
//		}

    }

    public void disableTabs(boolean generalTab, boolean mailTab, boolean faxTab, boolean letterTab, boolean phoneTab,
                            boolean sendTab, boolean sendReportTab) {
        getGeneralTab().setDisabled(generalTab);
        getMailTab().setDisabled(mailTab);
        getFaxTab().setDisabled(faxTab);
        getLetterTab().setDisabled(letterTab);
        getPhoneTab().setDisabled(phoneTab);
        getSendTab().setDisabled(sendTab);
        getSendReportTab().setDisabled(sendReportTab);
    }

    @Getter
    @Setter
    public abstract class NotificationTab extends AbstractTab {

        /**
         * True if notification method should be used
         */
        protected boolean useNotification;

        protected List<PrintDocument> templateList;

        protected DefaultTransformer<PrintDocument> templateListTransformer;

        protected PrintDocument selectedTemplate;

        /**
         * Updates the notification container list and if at least one notification for
         * this task should be performed useTab is set to true
         */
        @Override
        public void updateData() {
        }
    }

    @Getter
    @Setter
    public abstract class ContactTab extends NotificationTab {

        protected List<NotificationContainer> container = new ArrayList<NotificationContainer>();

        protected NotificationTyp notificationType;

        /**
         * True if individual address should be used for each contact
         */
        protected boolean individualAddresses;

        @Override
        public void updateData() {
//			List<NotificationContainer> newContainers = AssociatedContactService.getNotificationListForType(task,
//					notificationType, generalTab.isCompleteNotification());
//
//			List<NotificationContainer> foundContainer = new ArrayList<NotificationContainer>();
//
//			// adding new, saving found one
//			for (NotificationContainer notificationContainer : newContainers) {
//				if (getContainer().stream().anyMatch(p -> p.equals(notificationContainer))) {
//					// TODO update old conatienr
//				} else {
//					getContainer().add(notificationContainer);
//				}
//				foundContainer.add(notificationContainer);
//			}
//
//			// removing old container
//			newContainers.removeAll(foundContainer);
//			getContainer().removeAll(newContainers);
//
//			getContainer().stream().sorted((p1, p2) -> {
//				if (p1.isFaildPreviously() == p2.isFaildPreviously())
//					return 0;
//				else if (p1.isFaildPreviously())
//					return 1;
//				else
//					return -1;
//			});
        }

        /**
         * Returns only selected containers
         *
         * @return
         */
        public List<NotificationContainer> getContainerToNotify() {
            return container.stream().filter(p -> p.isPerform()).collect(Collectors.toList());
        }

    }

    @Getter
    @Setter
    public class GeneralTab extends NotificationTab {

        private List<DiagnosisRevision> diagnosisRevisions;

        private DiagnosisRevision selectDiagnosisRevision;

        private int printCount;

        /**
         * No diagnosis is selected, selection by user is required
         */
        private boolean selectDiagnosisManually;

        /**
         * Selected diagnosis is not approved
         */
        private boolean selectedDiagnosisNotApprovedy;

        /**
         * If true all contacts will be notified, even contacts that are not refreshed
         */
        private boolean completeNotification;

        public GeneralTab() {
            setTabName("GeneralTab");
            setName("dialog.notification.tab.general");
            setViewID("generalTab");
            setCenterInclude("include/general.xhtml");
        }

        @Override
        public boolean initTab() {
            // transforming diagnosis list
            diagnosisRevisions = new ArrayList<DiagnosisRevision>(task.getDiagnosisRevisions());

            selectDiagnosisRevision = diagnosisRevisions.stream()
                    .filter(p -> p.getNotificationStatus() == NotificationStatus.NOTIFICATION_PENDING).findFirst()
                    .orElse(null);

            useNotification = true;

            templateList = printDocumentRepository.findAllByTypes(DocumentType.DIAGNOSIS_REPORT,
                    DocumentType.DIAGNOSIS_REPORT_EXTERN);

            templateListTransformer = new DefaultTransformer<PrintDocument>(templateList);

            selectedTemplate = printDocumentRepository
                    .findByID(pathoConfig.getDefaultDocuments().getNotificationDefaultPrintDocument()).orElse(null);

            printCount = 2;

            completeNotification = false;

            // if no diagnosis is pending setting last diagnosis
            onDiangosisSelect();


            return true;
        }

        public void updateData() {
        }

        public void onDiangosisSelect() {
            if (getSelectDiagnosisRevision() == null) {
                setSelectDiagnosisManually(true);
                disableTabs(false, true, true, true, true, true, true);
                return;
            }

            if (selectDiagnosisManually) {
                selectDiagnosisManually = false;
                disableTabs(false, false, false, false, false, false, true);
            }

            if (getSelectDiagnosisRevision().getNotificationStatus() != NotificationStatus.NOTIFICATION_PENDING
                    || (getSelectDiagnosisRevision().getNotificationStatus() == NotificationStatus.NOTIFICATION_PENDING
                    && getSelectDiagnosisRevision().isNotified())) {
                selectedDiagnosisNotApprovedy = true;
            } else
            selectedDiagnosisNotApprovedy = false;

        }

    }

    @Getter
    @Setter
    public class MailTab extends ContactTab {

        private MailTemplate mailTemplate;

        public MailTab() {
            setTabName("MailTab");
            setName("dialog.notification.tab.mail");
            setViewID("mailTab");
            setCenterInclude("include/mail.xhtml");
            setNotificationType(NotificationTyp.EMAIL);
        }

        @Override
        public boolean initTab() {

            individualAddresses = false;

            templateList = printDocumentRepository.findAllByTypes(DocumentType.DIAGNOSIS_REPORT,
                    DocumentType.DIAGNOSIS_REPORT_EXTERN);

            templateListTransformer = new DefaultTransformer<PrintDocument>(templateList);

            selectedTemplate = printDocumentRepository
                    .findByID(pathoConfig.getDefaultDocuments().getNotificationDefaultEmailDocument()).orElse(null);

            mailTemplate = mailRepository.findByID(pathoConfig.getDefaultDocuments().getNotificationDefaultEmail())
                    .orElse(null);

            mailTemplate.initialize(new InitializeToken("patient", task.getPatient()), new InitializeToken("task", task),
                    new InitializeToken("contact", null));

            updateData();

            useNotification = getContainer().size() > 0;

            logger.debug("Mail data initialized");

            return super.initTab();
        }
    }

    @Getter
    @Setter
    public class FaxTab extends ContactTab {

        private boolean sendFax;

        private boolean printFax;

        public FaxTab() {
            setTabName("FaxTab");
            setName("dialog.notification.tab.fax");
            setViewID("faxTab");
            setCenterInclude("include/fax.xhtml");
            setNotificationType(NotificationTyp.FAX);
        }

        @Override
        public boolean initTab() {

            individualAddresses = true;

            templateList = printDocumentRepository.findAllByTypes(DocumentType.DIAGNOSIS_REPORT,
                    DocumentType.DIAGNOSIS_REPORT_EXTERN);

            templateListTransformer = new DefaultTransformer<PrintDocument>(templateList);

            selectedTemplate = printDocumentRepository
                    .findByID(pathoConfig.getDefaultDocuments().getNotificationDefaultFaxDocument()).orElse(null);

            sendFax = true;

            printFax = false;

            updateData();

            useNotification = getContainer().size() > 0;

            logger.debug("Fax data initialized");

            return super.initTab();
        }
    }

    @Getter
    @Setter
    public class LetterTab extends ContactTab {

        private boolean printLetter;

        public LetterTab() {
            setTabName("LetterTab");
            setName("dialog.notification.tab.letter");
            setViewID("letterTab");
            setCenterInclude("include/letter.xhtml");
            setNotificationType(NotificationTyp.LETTER);
        }

        @Override
        public boolean initTab() {
            individualAddresses = true;

            templateList = printDocumentRepository.findAllByTypes(DocumentType.DIAGNOSIS_REPORT,
                    DocumentType.DIAGNOSIS_REPORT_EXTERN);

            templateListTransformer = new DefaultTransformer<PrintDocument>(templateList);

            selectedTemplate = printDocumentRepository
                    .findByID(pathoConfig.getDefaultDocuments().getNotificationDefaultLetterDocument()).orElse(null);

            printLetter = true;

            updateData();

            useNotification = getContainer().size() > 0;

            logger.debug("Letter data initialized");

            return super.initTab();
        }
    }

    @Getter
    @Setter
    public class PhoneTab extends ContactTab {

        public PhoneTab() {
            setTabName("PhoneTab");
            setName("dialog.notification.tab.phone");
            setViewID("phoneTab");
            setCenterInclude("include/phone.xhtml");
            setNotificationType(NotificationTyp.PHONE);
        }

        @Override
        public boolean initTab() {

            updateData();

            useNotification = getContainer().size() > 0;

            logger.debug("Phone data initialized");
            return super.initTab();
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
            logger.debug("Update Data send");

//			ValidatorUtil val = new ValidatorUtil();
//			// checking mails
//			mailTab.getContainerToNotify().forEach(p -> {
//				if (!val.approveMailAddress(p.getContactAddress()))
//					p.setWarning(true, "dialog.notification.sendProcess.mail.error.mailNotValid");
//				else
//					p.clearWarning();
//			});
//
//			faxTab.getContainerToNotify().forEach(p -> {
//				if (!val.approveFaxAddress(p.getContactAddress()))
//					p.setWarning(true, "dialog.notification.sendProcess.fax.error.numberNotValid");
//				else
//					p.clearWarning();
//			});
        }

        // @Async("taskExecutor")
        public void performeNotification() {

            logger.debug("Startin notification thread");

//			try {
//				if (isNotificationRunning()) {
//					logger.debug("Thread allready running, abort new request!");
//					return;
//				}
//
//				setNotificationRunning(true);
//
//				setSteps(calculateSteps());
//
//				NotificationPerformer performer = new NotificationPerformer(getTask(),
//						generalTab.getSelectDiagnosisRevision());
//
//				performer.printNotification(generalTab.isUseNotification(), generalTab.getSelectedTemplate(),
//						generalTab.getPrintCount());
//
//				performer.mailNotification(mailTab.isUseNotification(), mailTab.getContainerToNotify(),
//						mailTab.isIndividualAddresses(), mailTab.getSelectedTemplate(), mailTab.getMailTemplate());
//
//				performer.faxNotification(faxTab.isUseNotification(), faxTab.getContainerToNotify(),
//						faxTab.isIndividualAddresses(), faxTab.isSendFax(), faxTab.isPrintFax(),
//						faxTab.getSelectedTemplate());
//
//				performer.letterNotification(letterTab.isUseNotification(), letterTab.getContainerToNotify(),
//						letterTab.getSelectedTemplate());
//
//				performer.phoneNotification(phoneTab.isUseNotification(), phoneTab.getContainerToNotify());
//
//				notificationService.performeNotification(performer, this);
//
//				sendReportTab.setCurrentSendReport(performer.getSendReport());
//
//				setProgressPercent(100);
//
//				// progressStepText("dialog.notification.sendProcess.success");
//
//				logger.debug("Messaging ended");
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

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

            steps += mailTab.isUseNotification() ? mailTab.getContainerToNotify().size() : 0;
            steps += faxTab.isUseNotification() ? faxTab.getContainerToNotify().size() : 0;
            steps += letterTab.isUseNotification() ? letterTab.getContainerToNotify().size() : 0;
            steps += phoneTab.isUseNotification() ? 1 : 0;

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
    public class SendReportTab extends NotificationTab implements PDFStreamContainer {

        private DefaultTransformer<PDFContainer> sendReportConverter;

        private List<PDFContainer> sendReportList;

        private PDFContainer currentSendReport;

        public SendReportTab() {
            setTabName("SendReport");
            setName("dialog.notification.tab.sendReport");
            setViewID("sendReportTab");
            setCenterInclude("include/sendReport.xhtml");
        }

        @Override
        public void updateData() {
            logger.debug("Update Data sendReport");
            // getting all sendreports
            sendReportList = PDFService.findPDFsByDocumentType(task.getAttachedPdfs(),
                    DocumentType.MEDICAL_FINDINGS_SEND_REPORT_COMPLETED);

            setSendReportConverter(new DefaultTransformer<>(sendReportList));
        }

        public void repeatNotification() {
            initBean(getTask(), true);
            onTabChange(generalTab);
        }

        public void onReturnDialog(SelectEvent event) {
            logger.debug("Dialog return");
            if (event.getObject() != null && event.getObject() instanceof Boolean
                    && ((Boolean) event.getObject()).booleanValue())
                hideDialog();
        }

        @Override
        public void setDisplayPDF(PDFContainer container) {
            currentSendReport = container;
        }

        @Override
        public PDFContainer getDisplayPDF() {
            return currentSendReport;
        }

        @Override
        public void setTooltip(PDFContainer container) {
        }

        @Override
        public PDFContainer getTooltip() {
            return null;
        }

        @Override
        public MediaRepository getMediaRepository() {
            return mediaRepository;
        }
    }

}
