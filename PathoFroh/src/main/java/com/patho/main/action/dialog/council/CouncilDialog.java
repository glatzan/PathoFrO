package com.patho.main.action.dialog.council;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.ContactRole;
import com.patho.main.common.CouncilState;
import com.patho.main.common.Dialog;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.common.SortOrder;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.Council;
import com.patho.main.model.ListItem;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.Physician;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.CouncilRepository;
import com.patho.main.repository.ListItemRepository;
import com.patho.main.repository.PhysicianRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.CouncilService;
import com.patho.main.service.PDFService;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.template.print.ui.document.AbstractDocumentUi;
import com.patho.main.template.print.ui.document.report.CouncilReportUi;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import javassist.Modifier;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

@Configurable
@Getter
@Setter
public class CouncilDialog extends AbstractDialog<CouncilDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ListItemRepository listItemRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PhysicianRepository physicianRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private CouncilService councilService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private CouncilRepository councilRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PrintDocumentRepository printDocumentRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private DialogHandler dialogHandler;

	/**
	 * Root node for Tree
	 */
	private TreeNode root;

	/**
	 * Selected Node
	 */
	private TreeNode selectedNode;

	/**
	 * Selected council from councilList
	 */
	private Council selectedCouncil;

	/**
	 * Council nodes
	 */
	private List<TreeNode> councilNodes;

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

	private boolean admendSelectedRequestState;

	/**
	 * Initializes the bean and shows the council dialog
	 * 
	 * @param task
	 */
	public CouncilDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	/**
	 * Initializes the bean and calles updatePhysicianLists at the end.
	 * 
	 * @param task
	 */
	public boolean initBean(Task task) {

		super.initBean(task, Dialog.COUNCIL);

		// reload task in order to load councils
		update(true);

		setCouncilList(new ArrayList<Council>(getTask().getCouncils()));

		// setting council as default
		if (getCouncilNodes().size() != 0) {
			setSelectedNode(getCouncilNodes().get(0).getChildren().get(0));
			setSelectedCouncil((Council) getCouncilNodes().get(0).getData());
		} else {
			setSelectedNode(null);
			setSelectedCouncil(null);
		}

		updatePhysicianLists();

		setAttachmentList(listItemRepository
				.findByListTypeAndArchivedOrderByIndexInListAsc(ListItem.StaticList.COUNCIL_ATTACHMENT, false));

		setEditable(task.getTaskStatus().isEditable());

		return true;
	}

	/**
	 * Updates the tree menu und reloads the patient's data
	 * 
	 * @param reloadPatient
	 */
	private void update(boolean reloadTask) {
		if (reloadTask) {
			setTask(taskRepository.findOptionalByIdAndInitialize(task.getId(), true, true, true, true, true).get());
			getTask().generateTaskStatus();
		}

		setRoot(generateTree(task));

		if (getSelectedNode() != null) {
			for (TreeNode node : getCouncilNodes()) {
				if (((Council) node.getData()).equals((getSelectedNode()).getData())) {
					setSelectedNode(node);
					setSelectedCouncil((Council) node.getData());
					return;
				}
			}
		}

		setSelectedNode(null);
		setSelectedCouncil(null);
	}

	private TreeNode generateTree(Task task) {

		logger.debug("Generating new tree");

		TreeNode root = new DefaultTreeNode("Root", null);

		TreeNode taskNode = new DefaultTreeNode("task", task, root);
		taskNode.setExpanded(true);
		taskNode.setSelectable(false);

		councilNodes = new ArrayList<TreeNode>();

		for (Council council : task.getCouncils()) {
			logger.debug("Creating tree for {}", council);
			TreeNode councilNode = new DefaultTreeNode("council", new CouncilContainer(council), taskNode);
			councilNode.setExpanded(true);
			councilNode.setSelectable(false);

			councilNodes.add(councilNode);

			TreeNode councilRequestNode = new DefaultTreeNode("council_request", new CouncilContainer(council),
					councilNode);
			councilRequestNode.setExpanded(true);
			councilRequestNode.setSelectable(true);

			if (council.isSampleShipped()) {
				TreeNode councilshipNode = new DefaultTreeNode("council_ship", new CouncilContainer(council),
						councilNode);
				councilshipNode.setExpanded(true);
				councilshipNode.setSelectable(true);
			}

			TreeNode councilReturnNode = new DefaultTreeNode("council_reply", new CouncilContainer(council),
					councilNode);
			councilReturnNode.setExpanded(true);
			councilReturnNode.setSelectable(true);

			TreeNode councilDataNode = new DefaultTreeNode("data_node", new CouncilContainer(council), councilNode);
			councilDataNode.setExpanded(true);
			councilDataNode.setSelectable(true);

			for (PDFContainer container : council.getAttachedPdfs()) {
				TreeNode councilFileNode = new DefaultTreeNode("file_node", container, councilDataNode);
				councilFileNode.setExpanded(false);
				councilFileNode.setSelectable(true);
			}
		}

		return root;
	}

	public void onNodeSelect() {
		if (getSelectedNode() != null) {
			setSelectedCouncil((Council) getSelectedNode().getData());
		}
	}

	public String getCenterInclude() {
		if (getSelectedNode() != null) {

			switch (getSelectedNode().getType()) {
			case "council_request":
				return "inculde/request.xhtml";
			case "council_ship":
				return "inculde/ship.xhtml";
			case "council_reply":
				return "inculde/reply.xhtml";
			default:
				return "inculde/empty.xhtml";
			}
		}

		return "inculde/empty.xhtml";
	}

	/**
	 * Renews the physician lists
	 */
	public void updatePhysicianLists() {
		// list of physicians which are the counselors
		setPhysicianCouncilList(physicianRepository.findAllByRole(new ContactRole[] { ContactRole.CASE_CONFERENCE },
				true, SortOrder.PRIORITY));
		setPhysicianCouncilTransformer(new DefaultTransformer<Physician>(getPhysicianCouncilList()));

		// list of physicians to sign the request
		setPhysicianSigantureList(physicianRepository.findAllByRole(new ContactRole[] { ContactRole.SIGNATURE }, true,
				SortOrder.PRIORITY));
		setPhysicianSigantureListTransformer(new DefaultTransformer<Physician>(getPhysicianSigantureList()));
	}

	/**
	 * Creates a new council and saves it
	 */
	public void createCouncil() {
		logger.info("Adding new council");
		councilService.createCouncil(getTask(), true).getTask();
		update(true);
	}

	public void admendRequestState() {
		setAdmendSelectedRequestState(true);
	}

	public void endRequestState(CouncilContainer council) {
		logger.debug("Ending request phase");
		councilService.endCouncilRequest(council.getCouncil(), null);
		System.out.println(council.getNotificationType());
		setAdmendSelectedRequestState(false);
		update(true);
		selectNextStep();
	}

	/**
	 * Selects the next step
	 */
	private void selectNextStep() {
		TreeNode parent = getSelectedNode().getParent();
		int index;
		if (parent.getChildCount() - 1 > (index = parent.getChildren().indexOf(getSelectedNode()))) {
			setSelectedNode(parent.getChildren().get(index + 1));
		}
	}

	/**
	 * Handels file upload
	 * 
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		try {
			logger.debug("Uploadgin to Council: " + getSelectedCouncil().getId());
			PDFReturn res = pdfService.createAndAttachPDF(getSelectedCouncil(), file, DocumentType.COUNCIL_REPLY, "",
					"", true, new File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN
							+ String.valueOf(getSelectedCouncil().getTask().getPatient().getId())));

			MessageHandler.sendGrowlMessagesAsResource("growl.upload.success");
		} catch (IllegalAccessError e) {
			MessageHandler.sendGrowlMessagesAsResource("growl.upload.failed", FacesMessage.SEVERITY_ERROR);
		}

		update(true);
	}

	public void onCouncilStateChange() {
		try {

			save();

			switch (getSelectedCouncil().getCouncilState()) {
			case EditState:
			case ValidetedState:
				logger.debug("EditState selected");
				// removing all fav lists
				removeListFromTask(PredefinedFavouriteList.CouncilSendRequestMTA,
						PredefinedFavouriteList.CouncilSendRequestSecretary, PredefinedFavouriteList.CouncilRequest,
						PredefinedFavouriteList.CouncilCompleted);
				break;
			case LendingStateMTA:
			case LendingStateSecretary:
				logger.debug("LendingState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilRequest, PredefinedFavouriteList.CouncilCompleted);
				favouriteListDAO.addReattachedTaskToList(getTask(),
						getSelectedCouncil().getCouncilState() == CouncilState.LendingStateMTA
								? PredefinedFavouriteList.CouncilSendRequestMTA
								: PredefinedFavouriteList.CouncilSendRequestSecretary);
				break;
			case PendingState:
				logger.debug("PendingState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilSendRequestMTA,
						PredefinedFavouriteList.CouncilSendRequestSecretary, PredefinedFavouriteList.CouncilCompleted);
				favouriteListDAO.addReattachedTaskToList(getTask(), PredefinedFavouriteList.CouncilRequest);
				break;
			case CompletedState:
				logger.debug("CompletedState selected");
				// removing pending and completed state
				removeListFromTask(PredefinedFavouriteList.CouncilSendRequestMTA,
						PredefinedFavouriteList.CouncilSendRequestSecretary, PredefinedFavouriteList.CouncilRequest);
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
					logger.debug("Not removing from CouncilCompleted list, other councils are in this state");
				break;
			case CouncilRequest:
				if (!getTask().getCouncils().stream().anyMatch(p -> p.getCouncilState() == CouncilState.PendingState))
					favouriteListDAO.removeReattachedTaskFromList(getTask(), predefinedFavouriteList);
				else
					logger.debug("Not removing from CouncilPending list, other councils are in this state");
				break;
			case CouncilSendRequestMTA:
			case CouncilSendRequestSecretary:
				if (!getTask().getCouncils().stream().anyMatch(p -> p.getCouncilState() == CouncilState.LendingStateMTA
						|| p.getCouncilState() == CouncilState.LendingStateSecretary))
					favouriteListDAO.removeReattachedTaskFromList(getTask(), predefinedFavouriteList);
				else
					logger.debug("Not removing from CouncilLendingMTA list, other councils are in this state");
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Updates the council name
	 */
	public void onNameChange() {
		getSelectedCouncil().setName(councilService.generateCouncilName(getSelectedCouncil()));
		saveSelectedCouncil();
	}

	public void onSendSlides() {
		saveSelectedCouncil();
		update(true);
	}

	/**
	 * Saves the currently selected coucil and replaces all old objects
	 */
	public void saveSelectedCouncil() {
		logger.debug("Saving council data");
		if (getSelectedCouncil() != null) {
			Council c = councilRepository.save(getSelectedCouncil(),
					resourceBundle.get("log.patient.task.council.update", getTask(), getSelectedCouncil().getName()),
					task.getPatient());

			// replacing old coucils
			DefaultTreeNode parent = (DefaultTreeNode) getSelectedNode().getParent();
			parent.setData(c);

			for (TreeNode node : parent.getChildren()) {
				((DefaultTreeNode) node).setData(c);
			}

			setSelectedCouncil(c);
		}
	}

	/**
	 * hideDialog should be called first. This method opens a printer dialog, an let
	 * the gui click the button for opening the dialog. This is a workaround for
	 * opening other dialogs after closing the current dialog.
	 */
	public void printCouncilReport() {

		List<PrintDocument> printDocuments = printDocumentRepository.findAllByTypes(DocumentType.COUNCIL_REQUEST);

		// getting ui objects
		List<AbstractDocumentUi<?, ?>> printDocumentUIs = AbstractDocumentUi.factory(printDocuments);

		for (AbstractDocumentUi<?, ?> documentUi : printDocumentUIs) {
			((CouncilReportUi) documentUi).initialize(task, getSelectedCouncil());
			((CouncilReportUi) documentUi).setRenderSelectedContact(true);
			((CouncilReportUi) documentUi).setUpdatePdfOnEverySettingChange(true);
			((CouncilReportUi) documentUi).getSharedData().setSingleSelect(true);
		}

		dialogHandler.getPrintDialog().initAndPrepareBean(getTask(), printDocumentUIs, printDocumentUIs.get(0));
	}

	public void showMediaSelectDialog() {
		showMediaSelectDialog(null);
	}

	public void showMediaSelectDialog(PDFContainer pdf) {
		try {

			// init dialog for patient and task
			dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(),
					new DataList[] { getTask(), getTask().getPatient() }, pdf, true);

			// setting advance copy mode with move as true and target to task
			// and biobank
			dialogHandlerAction.getMediaDialog().enableAutoCopyMode(new DataList[] { getTask(), getSelectedCouncil() },
					true, true);

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
		dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(), getSelectedCouncil(), pdfContainer,
				false);

		// setting info text
		dialogHandlerAction.getMediaDialog().setActionDescription(
				resourceBundle.get("dialog.pdfOrganizer.headline.info.council", getTask().getTaskID()));

		// show dialog
		dialogHandlerAction.getMediaDialog().prepareDialog();
	}

	@Getter
	@Setter
	public static class CouncilContainer extends Council {

		private CouncilNotificationType notificationType = CouncilNotificationType.MTA;

		@Delegate
		@Setter(AccessLevel.NONE)
		private Council council;

		public CouncilContainer(Council council) {
			this.council = council;
		}

		public static enum CouncilNotificationType {
			MTA, SECRETARY;
		}
	}
}
