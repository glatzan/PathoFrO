package com.patho.main.action.dialog.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.primefaces.model.menu.MenuModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.patho.main.action.DialogHandlerAction;
import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.ContactRole;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.Dialog;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.common.InformedConsentType;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.common.TaskPriority;
import com.patho.main.model.BioBank;
import com.patho.main.model.Council;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.DiagnosisService;
import com.patho.main.service.SampleService;
import com.patho.main.service.TaskService;

import com.patho.main.template.print.CaseCertificate;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.helper.TimeUtil;
import com.patho.main.util.pdf.PDFGenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class CreateTaskDialog extends AbstractDialog {

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
	private TaskService taskService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MaterialPresetRepository materialPresetRepository;
	
	private Patient patient;

	private List<MaterialPreset> materialList;

	private int sampleCount;

	private boolean autoNomenclatureChangedManually;

	private BioBank bioBank;

	private boolean moveInformedConsent;

	private boolean taskIdManuallyAltered;

	private boolean taskIDisPresentInDatabase;

	/**
	 * True if external Task and there are sample for returning to client
	 */
	private boolean externalTask;

	/**
	 * Commentary for returning the samples
	 */
	private String exneralCommentary;

	/**
	 * Initializes the bean and shows the createTaskDialog
	 * 
	 * @param patient
	 */
	public void initAndPrepareBean(Patient patient) {
		initBean(patient);
		prepareDialog();
	}

	/**
	 * Initializes the bean
	 * 
	 * @param patient
	 */
	public void initBean(Patient patient) {
		super.initBean(new Task(getPatient()), Dialog.TASK_CREATE, true);

		// setting material list
		setMaterialList(materialPresetRepository.findAll(true));

		getTask().setTaskID(getNewTaskID());
		getTask().setTaskPriority(TaskPriority.NONE);
		getTask().setDateOfReceipt(TimeUtil.setDayBeginning(System.currentTimeMillis()));
		getTask().setUseAutoNomenclature(true);
		getTask().setSamples(new ArrayList<Sample>());

		// samplecount for new task
		setSampleCount(1);

		setAutoNomenclatureChangedManually(false);

		setTaskIdManuallyAltered(false);
		setTaskIDisPresentInDatabase(false);

		Sample newSample = new Sample();
		newSample.setCreationDate(System.currentTimeMillis());
		newSample.setParent(getTask());
		newSample.setMaterialPreset(getMaterialList().get(0));
		newSample.setMaterial(getMaterialList().get(0).getName());
		getTask().getSamples().add(newSample);
		getTask().updateAllNames();

		// setting biobank
		setBioBank(new BioBank());
		getBioBank().setInformedConsentType(InformedConsentType.NONE);
		getBioBank().setTask(getTask());

		// resetting selected container
		// dialogHandlerAction.getMediaDialog().setSelectedPdfContainer(null);

		setExneralCommentary("");
		setExternalTask(false);

		setMoveInformedConsent(false);
	}

	/**
	 * Updates the name and the amount of samples which should be created with the
	 * new task.
	 */
	public void updateDialog() {

		logger.debug("New Task: Updating sample tree");

		// changing autoNomeclature of samples, if no change was made manually
		if (getSampleCount() > 1 && !isAutoNomenclatureChangedManually()) {
			logger.debug("New Task: More then one sample, setting autonomeclature to true");
			getTask().setUseAutoNomenclature(true);
		} else if (getSampleCount() == 1 && !isAutoNomenclatureChangedManually()) {
			logger.debug("New Task: Only one sample, setting autonomeclature to false");
			getTask().setUseAutoNomenclature(false);
		}

		if (getSampleCount() >= 1) {
			if (getSampleCount() > task.getSamples().size()) {
				logger.debug("New Task: Samplecount > samples, adding new samples");
				// adding samples if count is bigger then the current sample
				// count
				while (getSampleCount() > task.getSamples().size()) {
					Sample newSample = new Sample();
					newSample.setCreationDate(System.currentTimeMillis());
					newSample.setParent(getTask());
					newSample.setMaterialPreset(getMaterialList().get(0));
					newSample.setMaterial(getMaterialList().get(0).getName());
					getTask().getSamples().add(newSample);
					getTask().updateAllNames();
				}
			} else if (getSampleCount() < task.getSamples().size()) {
				logger.debug("New Task: Samplecount > samples, removing sample");
				// removing samples if count is less then current sample count
				while (getSampleCount() < task.getSamples().size())
					task.getSamples().remove(task.getSamples().size() - 1);
			}
		}

		logger.debug("New Task: Updating sample names");

		// updates the name of all other samples
		for (Sample sample : task.getSamples()) {
			sample.updateNameOfSample(getTask().isUseAutoNomenclature(), true);
		}
	}

	/**
	 * Creates a new Task object and calls createBiobak at the end.
	 * 
	 * @throws CustomNotUniqueReqest
	 */
	public void createTask() {
		uniqueRequestID.checkUniqueRequestID(true);

		try {

			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				public void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

					genericDAO.reattach(getPatient());

					if (getPatient().getTasks() == null) {
						getPatient().setTasks(new ArrayList<>());
					}

					logger.debug("Creating new Task");

					getPatient().getTasks().add(0, getTask());
					// sets the new task as the selected task

					getTask().setParent(getPatient());
					getTask().setCaseHistory("");
					getTask().setWard("");
					getTask().setInsurance(patient.getInsurance());

					if (isTaskIdManuallyAltered()) {
						// TODO check if task id exists
					} else {
						// renewing taskID, if somebody has created an other
						// task in the meanwhile
						getTask().setTaskID(getNewTaskID());
					}

					getTask().setCouncils(new ArrayList<Council>());

					getTask().setFavouriteLists(new ArrayList<FavouriteList>());

					// saving task
					genericDAO.savePatientData(getTask(), "log.patient.task.new", getTask().getTaskID());

					getTask().setDiagnosisRevisions(new ArrayList<DiagnosisRevision>());

					for (Sample sample : getTask().getSamples()) {
						// saving samples
						genericDAO.savePatientData(sample, "log.task.sample.new", sample.getSampleID());
						// creating needed blocks
						sampleService.createBlock(sample);

					}

					logger.debug("Creating diagnosis");
					// creating standard diagnoses
					diagnosisService.createDiagnosisRevision(getTask(), DiagnosisRevisionType.DIAGNOSIS);

					// creating bioBank for Task
					bioBank.setAttachedPdfs(new ArrayList<PDFContainer>());

					PDFContainer selectedPDF = dialogHandlerAction.getMediaDialog().getSelectedPdfContainer();

					if (selectedPDF != null) {
						// attaching pdf to biobank
						bioBank.getAttachedPdfs().add(selectedPDF);

						genericDAO.savePatientData(bioBank, getTask(), "log.patient.bioBank.pdf.attached",
								selectedPDF.getName());

						// and task
						getTask().setAttachedPdfs(new ArrayList<PDFContainer>());
						getTask().getAttachedPdfs().add(selectedPDF);

						genericDAO.savePatientData(getTask(), "log.pdf.attached");

						if (isMoveInformedConsent()) {
							patient.getAttachedPdfs().remove(selectedPDF);
							genericDAO.savePatientData(getPatient(), "log.patient.pdf.removed", selectedPDF.getName());
						}
					} else {
						genericDAO.savePatientData(bioBank, getTask(), "log.patient.bioBank.save");
					}

					// adding patient to the contact list
					contactDAO.addAssociatedContact(task, getPatient().getPerson(), ContactRole.PATIENT);

					genericDAO.savePatientData(getTask(), "log.patient.task.update", task.getTaskID());

					favouriteListDAO.addTaskToList(getTask(), PredefinedFavouriteList.StainingList.getId());

					if (externalTask) {
						favouriteListDAO.addTaskToList(task, PredefinedFavouriteList.ReturnSampleList.getId(),
								exneralCommentary);
					}

					genericDAO.save(task.getPatient());

				}
			});

			genericDAO.lockParent(task);

		} catch (Exception e) {
			getPatient().getTasks().remove(0);
			onDatabaseVersionConflict();
		}
	}

	/**
	 * Calls createTask and prints the Ureport form
	 * 
	 * @throws CustomNotUniqueReqest
	 */
	public void createTaskAndPrintUReport() {
		createTask();

		CaseCertificate uReport = DocumentTemplate
				.getTemplateByID(globalSettings.getDefaultDocuments().getTaskCreationDocument());

		if (uReport == null) {
			logger.error("New Task: No TemplateUtil for printing UReport found");
			return;
		}

		// printing u report
		uReport.initData(task);
		PDFContainer newPdf = new PDFGenerator().getPDF(uReport);

		logger.debug("printing task page");
		userHandlerAction.getSelectedPrinter().print(newPdf, uReport);
	}

	/**
	 * Is called if the user has manually change the checkbox, after that the
	 * autonomeclature settings aren't changed automatically
	 */
	public void manuallyChangeAutoNomenclature() {
		logger.debug("New Task: Autonomeclature change manually");
		setAutoNomenclatureChangedManually(true);
		updateDialog();
	}

	/**
	 * Returns the name for a new Task
	 * 
	 * @return
	 */
	public String getNewTaskID() {
		// generating new task id
		Task task = taskRepository.findByLastID(Calendar.getInstance(), false, false, false, false, false).get();

		// task is within the current year
		if (task != null) {
			// getting counter
			String count = task.getTaskID().substring(2, 6);
			// increment counter
			int counterAsInt = Integer.valueOf(count) + 1;
			return Integer.toString(TimeUtil.getCurrentYear() - 2000) + HistoUtil.fitString(counterAsInt, 4, '0');
		} else {
			// first task ever, or first task of year , year + 0001
			return Integer.toString(TimeUtil.getCurrentYear() - 2000) + HistoUtil.fitString(1, 4, '0');
		}

	}

	public void showMediaSelectDialog(PDFContainer pdfContainer) {
		// init dialog for patient and task
		dialogHandlerAction.getMediaDialog().initBean(getPatient(), new DataList[] { getPatient() }, pdfContainer,
				true);

		// enabeling upload to task
		dialogHandlerAction.getMediaDialog().enableUpload(new DataList[] { getPatient() },
				new DocumentType[] { DocumentType.BIOBANK_INFORMED_CONSENT });

		// setting info text
		dialogHandlerAction.getMediaDialog()
				.setActionDescription(resourceBundle.get("dialog.pdfOrganizer.headline.info.biobank", getTask().getTaskID()));

		// show dialog
		dialogHandlerAction.getMediaDialog().prepareDialog();
	}

	public void validateTaskID(FacesContext context, UIComponent componentToValidate, Object value)
			throws ValidatorException {

		if (value == null || value.toString().length() != 6) {
			throw new ValidatorException(
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Auftragsnummer muss sechs Zahlen enthalten."));
		} else if (!value.toString().matches("[0-9]{6}")) {
			throw new ValidatorException(
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Auftragsnummer darf nur Zahlen enthalten"));
		} else if (taskRepository.findOptionalByTaskId(value.toString()).isPresent()) {
			throw new ValidatorException(
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Auftragsnummer bereits vorhanden"));
		}
	}

	public void onMaterialPresetChange(Sample sample) {
		sample.setMaterial(sample.getMaterialPreset() != null ? sample.getMaterialPreset().getName() : "");
	}

}
