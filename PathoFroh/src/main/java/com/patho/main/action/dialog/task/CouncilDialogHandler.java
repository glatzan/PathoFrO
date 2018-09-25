package com.patho.main.action.dialog.task;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.DialogHandlerAction;
import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.ContactRole;
import com.patho.main.common.CouncilState;
import com.patho.main.common.DateFormat;
import com.patho.main.common.Dialog;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.Council;
import com.patho.main.model.ListItem;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Physician;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Task;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.template.print.ui.document.report.CouncilReportUi;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value = "session")
@Getter
@Setter
@Slf4j
public class CouncilDialogHandler extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DialogHandlerAction dialogHandlerAction;


	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;


	/**
	 * Selected council from councilList
	 */
	private Council selectedCouncil;

	/**
	 * List of all councils of this tasks
	 */
	private List<Council> councilList;

	/**
	 * List of physician to address a council
	 */
	private List<Physician> physicianCouncilList;

	/**
	 * Transformer for phyisicianCouncilList
	 */
	private DefaultTransformer<Physician> physicianCouncilTransformer;

	/**
	 * List of physicians to sign the request
	 */
	private List<Physician> physicianSigantureList;

	/**
	 * Transformer for physicianSiangotureList
	 */
	private DefaultTransformer<Physician> physicianSigantureListTransformer;

	/**
	 * Contains all available attachments
	 */
	private List<ListItem> attachmentList;

	/**
	 * True if editable
	 */
	private boolean editable;
	
	/**
	 * Initializes the bean and shows the council dialog
	 * 
	 * @param task
	 */
	public void initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
	}

	/**
	 * Initializes the bean and calles updatePhysicianLists at the end.
	 * 
	 * @param task
	 */
	public boolean initBean(Task task) {
		try {
			taskDAO.initializeTask(task, true);
			taskDAO.initializeCouncils(task);

			super.initBean(task, Dialog.COUNCIL);

			setCouncilList(new ArrayList<Council>(getTask().getCouncils()));

			// setting council as default
			if (getCouncilList().size() != 0) {
				setSelectedCouncil(getCouncilList().get(0));
			}else
				setSelectedCouncil(null);

			updatePhysicianLists();

			setAttachmentList(utilDAO.getAllStaticListItems(ListItem.StaticList.COUNCIL_ATTACHMENT));

			setEditable(task.getTaskStatus().isEditable());

			return true;
		} catch (HistoDatabaseInconsistentVersionException e) {
			log.debug("Version conflict, updating entity");
			task = taskDAO.getTaskAndPatientInitialized(task.getId());
			worklistViewHandlerAction.replaceTaskInCurrentWorklist(task, false);
			return false;
		}
	}

	/**
	 * Renews the physician lists
	 */
	public void updatePhysicianLists() {
		// list of physicians which are the counselors
		setPhysicianCouncilList(physicianDao.list(ContactRole.CASE_CONFERENCE, false));
		setPhysicianCouncilTransformer(new DefaultTransformer<Physician>(getPhysicianCouncilList()));

		// list of physicians to sign the request
		setPhysicianSigantureList(physicianDao.list(ContactRole.SIGNATURE, false));
		setPhysicianSigantureListTransformer(new DefaultTransformer<Physician>(getPhysicianSigantureList()));
	}

	/**
	 * Creates a new council and saves it
	 */
	public void addNewCouncil() {
		log.info("Adding new council");
		
		setSelectedCouncil(new Council(getTask()));
		getSelectedCouncil().setDateOfRequest(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
		getSelectedCouncil().setName(generateName());
		getSelectedCouncil().setCouncilState(CouncilState.EditState);
		getSelectedCouncil().setAttachedPdfs(new ArrayList<PDFContainer>());

		saveCouncilData();
	}

	public void onCouncilStateChange() {
		try {

			save();

			switch (getSelectedCouncil().getCouncilState()) {
			case EditState:
			case ValidetedState:
				log.debug("EditState selected");
				// removing all fav lists
				removeListFromTask(PredefinedFavouriteList.CouncilLendingMTA,
						PredefinedFavouriteList.CouncilLendingSecretary, PredefinedFavouriteList.CouncilPending,
						PredefinedFavouriteList.CouncilCompleted);
				break;
			case LendingStateMTA:
			case LendingStateSecretary:
				log.debug("LendingState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilPending, PredefinedFavouriteList.CouncilCompleted);
				favouriteListDAO.addReattachedTaskToList(getTask(),
						getSelectedCouncil().getCouncilState() == CouncilState.LendingStateMTA
								? PredefinedFavouriteList.CouncilLendingMTA
								: PredefinedFavouriteList.CouncilLendingSecretary);
				break;
			case PendingState:
				log.debug("PendingState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilLendingMTA,
						PredefinedFavouriteList.CouncilLendingSecretary, PredefinedFavouriteList.CouncilCompleted);
				favouriteListDAO.addReattachedTaskToList(getTask(), PredefinedFavouriteList.CouncilPending);
				break;
			case CompletedState:
				log.debug("CompletedState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilLendingMTA,
						PredefinedFavouriteList.CouncilLendingSecretary, PredefinedFavouriteList.CouncilPending);
				favouriteListDAO.addReattachedTaskToList(getTask(), PredefinedFavouriteList.CouncilCompleted);
				break;
			default:
				break;
			}
		} catch (HistoDatabaseInconsistentVersionException e) {
			onCouncilStateChange();
		}

	}

	public void removeListFromTask(PredefinedFavouriteList... predefinedFavouriteLists)
			throws HistoDatabaseInconsistentVersionException {

		for (PredefinedFavouriteList predefinedFavouriteList : predefinedFavouriteLists) {
			switch (predefinedFavouriteList) {
			case CouncilCompleted:
				if (!getTask().getCouncils().stream().anyMatch(p -> p.getCouncilState() == CouncilState.CompletedState))
					favouriteListDAO.removeReattachedTaskFromList(getTask(), predefinedFavouriteList);
				else
					log.debug("Not removing from CouncilCompleted list, other councils are in this state");
				break;
			case CouncilPending:
				if (!getTask().getCouncils().stream().anyMatch(p -> p.getCouncilState() == CouncilState.PendingState))
					favouriteListDAO.removeReattachedTaskFromList(getTask(), predefinedFavouriteList);
				else
					log.debug("Not removing from CouncilPending list, other councils are in this state");
				break;
			case CouncilLendingMTA:
			case CouncilLendingSecretary:
				if (!getTask().getCouncils().stream().anyMatch(p -> p.getCouncilState() == CouncilState.LendingStateMTA
						|| p.getCouncilState() == CouncilState.LendingStateSecretary))
					favouriteListDAO.removeReattachedTaskFromList(getTask(), predefinedFavouriteList);
				else
					log.debug("Not removing from CouncilLendingMTA list, other councils are in this state");
				break;
			default:
				break;
			}
		}
	}

	public void onNameChange() {
		getSelectedCouncil().setName(generateName());
		saveCouncilData();
	}

	public String generateName() {
		StringBuffer str = new StringBuffer();

		// name
		if (getSelectedCouncil().getCouncilPhysician() != null)
			str.append(getSelectedCouncil().getCouncilPhysician().getPerson().getFullName());
		else
			str.append(resourceBundle.get("dialog.council.data.newCouncil"));

		str.append(" ");

		
		LocalDateTime ldt = LocalDateTime.ofInstant(selectedCouncil.getDateOfRequest().toInstant(), ZoneId.systemDefault());
		
		// adding date
		str.append(ldt.format(DateTimeFormatter.ofPattern(DateFormat.GERMAN_DATE.getDateFormat())));

		return str.toString();
	}

	public void saveCouncilData() {
		try {
			if (getSelectedCouncil() != null)
				save();
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	/**
	 * Saves a council. If id=0, the council is new and is added to the task, if
	 * id!=0 the council will only be saved.
	 * 
	 * @throws HistoDatabaseInconsistentVersionException
	 */
	private boolean save() throws HistoDatabaseInconsistentVersionException {
		// new
		if (getSelectedCouncil().getId() == 0) {
			log.debug("Council Dialog: Creating new council");
			// TODO: Better loggin
			genericDAO.savePatientData(getSelectedCouncil(), getTask(),
					"log.patient.task.council.create");

			task.getCouncils().add(getSelectedCouncil());

			genericDAO.savePatientData(getTask(), "log.patient.task.council.attached",
					String.valueOf(getSelectedCouncil().getId()));

		} else {
			log.debug("Council Dialog: Saving council");
			genericDAO.savePatientData(getSelectedCouncil(), getTask(),
					"log.patient.task.council.update", String.valueOf(getSelectedCouncil().getId()));
		}

		// updating council list
		setCouncilList(new ArrayList<Council>(getTask().getCouncils()));
		return true;
	}

	/**
	 * hideDialog should be called first. This method opens a printer dialog, an
	 * let the gui click the button for opening the dialog. This is a workaround
	 * for opening other dialogs after closing the current dialog.
	 */
	public void printCouncilReport() {
		try {
			save();
			
			List<DocumentTemplate> templates = DocumentTemplate.getTemplates(DocumentType.COUNCIL_REQUEST);
			List<AbstractDocumentUi<?>> subSelectUIs = templates.stream().map(p -> p.getDocumentUi()).collect(Collectors.toList());

			for (AbstractDocumentUi<?> documentUi : subSelectUIs) {
				((CouncilReportUi) documentUi).initialize(task, getSelectedCouncil());
				((CouncilReportUi) documentUi).setRenderSelectedContact(true);
				((CouncilReportUi) documentUi).setUpdatePdfOnEverySettingChange(true);
				((CouncilReportUi) documentUi).setSingleSelect(true);
			}

			dialogHandlerAction.getPrintDialog().initBeanForPrinting(task, subSelectUIs, DocumentType.COUNCIL_REQUEST);
			dialogHandlerAction.getPrintDialog().prepareDialog();

			// workaround for showing and hiding two dialogues
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	public void showMediaSelectDialog() {
		showMediaSelectDialog(null);
	}

	public void showMediaSelectDialog(PDFContainer pdf) {
		try {
			
			// init dialog for patient and task
			dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(), new DataList[] { getTask(), getTask().getPatient() }, pdf,
					true);

			// setting advance copy mode with move as true and target to task
			// and biobank
			dialogHandlerAction.getMediaDialog().enableAutoCopyMode(new DataList[] { getTask(), getSelectedCouncil() }, true, true);

			// enabeling upload to task
			dialogHandlerAction.getMediaDialog().enableUpload(new DataList[] { getTask() },
					new DocumentType[] { DocumentType.COUNCIL_REPLY });

			// setting info text
			dialogHandlerAction.getMediaDialog().setActionDescription(
					resourceBundle.get("dialog.pdfOrganizer.headline.info.council", getTask().getTaskID()));

			// show dialog
			dialogHandlerAction.getMediaDialog().prepareDialog();
		} catch (HistoDatabaseInconsistentVersionException e) {
			// do nothing
			// TODO: infom user
		}
	}

	public void showMediaViewDialog(PDFContainer pdfContainer) {
		// init dialog for patient and task
		dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(), getSelectedCouncil(), pdfContainer, false);

		// setting info text
		dialogHandlerAction.getMediaDialog()
				.setActionDescription(resourceBundle.get("dialog.pdfOrganizer.headline.info.council", getTask().getTaskID()));

		// show dialog
		dialogHandlerAction.getMediaDialog().prepareDialog();
	}

}
