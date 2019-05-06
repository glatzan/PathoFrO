package com.patho.main.action.dialog.media;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;

import com.patho.main.model.patient.miscellaneous.BioBank;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.BioBankRepository;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.PDFService;
import com.patho.main.service.PDFService.PDFInfo;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.ui.pdf.PDFStreamContainerImpl;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Configurable
@Getter
@Setter
public class PDFOrganizer extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PatientRepository patientRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private BioBankRepository bankRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Lazy
	private DialogHandler dialogHandler;

	private Patient patient;

	private TreeNode root;

	private TreeNode selectedNode;

	private PDFStreamContainerImpl streamContainer;

	private List<DataList> dataLists;

	private boolean enablePDFSelection;

	private boolean viewOnly;

	@Accessors(chain = true)
	private PrintDocumentType uploadDocumentType;

	@Accessors(chain = true)
	private DataList uploadTarget;

	public PDFOrganizer initAndPrepareBean(Patient patient) {
		if (initBean(patient))
			prepareDialog();
		return this;
	}

	public boolean initBean(Patient patient) {
		setPatient(patient);

		setStreamContainer(new PDFStreamContainerImpl());

		setSelectedNode(null);
		setDataLists(new ArrayList<DataList>());
		setRoot(null);

		update(true);

		this.enablePDFSelection = false;
		this.viewOnly = false;

		uploadDocumentType = PrintDocumentType.OTHER;
		uploadTarget = patient;

		super.initBean(null, Dialog.PDF_ORGANIZER);

		return true;
	}

	public PDFOrganizer setPDFToDisplay(PDFContainer c) {
		getStreamContainer().setDisplayPDF(c);
		return this;
	}

	public PDFOrganizer selectMode() {
		this.enablePDFSelection = true;
		return this;
	}

	public PDFOrganizer viewMode() {
		this.viewOnly = true;
		return this;
	}

	/**
	 * Updates the tree menu und reloads the patient's data
	 * 
	 * @param reloadPatient
	 */
	public void update(boolean reloadPatient) {
		if (reloadPatient)
			setPatient(patientRepository.findOptionalById(getPatient().getId(), true).get());

		setRoot(generateTree(patient));

		// unloading pdf is it was selected
		if (getStreamContainer().getDisplayPDF() != null
				&& PDFService.getParentOfPDF(getDataLists(), getStreamContainer().getDisplayPDF()) == null) {
			setSelectedNode(null);
			getStreamContainer().setDisplayPDF(null);
		}
	}

	/**
	 * Generates the menu tree
	 * 
	 * @param patient
	 * @return
	 */
	private TreeNode generateTree(Patient patient) {
		getDataLists().clear();
		TreeNode root = new DefaultTreeNode("Root", null);

		patient = pdfService.initializeDataListTree(patient);

		TreeNode patientNode = new DefaultTreeNode("dropAble_Patient", patient, root);
		patientNode.setExpanded(true);
		patientNode.setSelectable(false);

		getDataLists().add(patient);

		for (PDFContainer container : patient.getAttachedPdfs()) {
			new DefaultTreeNode("pdf", container, patientNode);
		}

		for (Task task : patient.getTasks()) {
			TreeNode taskNode = new DefaultTreeNode("dropAble_Task", task, patientNode);
			taskNode.setExpanded(true);
			taskNode.setSelectable(false);

			getDataLists().add(task);

			for (PDFContainer container : task.getAttachedPdfs()) {
				new DefaultTreeNode("pdf", container, taskNode);
			}

			Optional<BioBank> b = bankRepository.findOptionalByTaskAndInitialize(task, true, true);

			if (b.isPresent()) {
				getDataLists().add(b.get());
				TreeNode bioBankNode = new DefaultTreeNode("dropAble_Biobank", b.get(), taskNode);
				bioBankNode.setExpanded(true);
				bioBankNode.setSelectable(false);

				for (PDFContainer container : b.get().getAttachedPdfs()) {
					new DefaultTreeNode("pdf", container, bioBankNode);
				}
			}

		}

		return root;
	}

	/**
	 * Method is called an tree node select, copies the selected pdf container
	 */
	public void displaySelectedContainer() {
		logger.debug("Display selected container");
		getStreamContainer()
				.setDisplayPDF(getSelectedNode() != null ? (PDFContainer) getSelectedNode().getData() : null);
	}

	/**
	 * Eventhandler for moving pdfs
	 * 
	 * @param event
	 */
	public void onDragDrop(TreeDragDropEvent event) {
		TreeNode dragNode = event.getDragNode();
		TreeNode dropNode = event.getDropNode();

		// do not move if is not a pdf, parent is not dropable and pdf is not restricted
		if (!(dragNode.getData() instanceof PDFContainer) || !dropNode.getType().startsWith("dropAble")
				|| ((PDFContainer) dragNode.getData()).getRestricted()) {
			logger.debug("Cannot move PDf undo change");
			update(true);
			return;
		}

		PDFContainer pdf = (PDFContainer) dragNode.getData();
		DataList from = PDFService.getParentOfPDF(getDataLists(), pdf);
		DataList to = (DataList) dropNode.getData();

		pdfService.movePdf(from, to, pdf);

		logger.debug("Moving PDF");

		update(true);

		MessageHandler.sendGrowlMessagesAsResource("growl.pdf.move", "growl.pdf.move.text",
				new Object[] { pdf.getName() });
	}

	/**
	 * Handels file upload
	 * 
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		try {
			logger.debug("Uploadgin to Patient: " + getPatient().getId());

			DataList uploadList = PDFService.getDatalistFromDatalists(getDataLists(), getUploadTarget());

			if (uploadList == null) {
				MessageHandler.sendGrowlMessagesAsResource("growl.upload.noTarget");
				return;
			}

			PDFReturn res = pdfService.createAndAttachPDF(uploadList,
					new PDFInfo(file.getFileName(), uploadDocumentType), file.getContents(), true);

			MessageHandler.sendGrowlMessagesAsResource("growl.upload.success");
		} catch (IllegalAccessError e) {
			MessageHandler.sendGrowlMessagesAsResource("growl.upload.failed", FacesMessage.SEVERITY_ERROR);
		} catch (FileNotFoundException e) {
			MessageHandler.sendGrowlMessagesAsResource("growl.upload.failed", FacesMessage.SEVERITY_ERROR);
		}

		update(true);
	}

	/**
	 * Show print success notification
	 * 
	 * @param success
	 */
	public void displayPrintNotification(boolean success) {
		MessageHandler.sendGrowlMessagesAsResource("growl.print.printing",
				success ? "growl.print.success" : "growl.print.error.printError",
				success ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR);
	}

	/**
	 * Deletes the pdf of the current tree node
	 */
	public void deletePDFContainer() {
		if (!((PDFContainer) getSelectedNode().getData()).getRestricted())
			deletePDFContainer((PDFContainer) getSelectedNode().getData());
		else
			MessageHandler.sendGrowlMessagesAsResource("growl.pdf.delete.forbidden");
	}

	/**
	 * Deletes the passed pdf container
	 * 
	 * @param container
	 */
	public void deletePDFContainer(PDFContainer container) {
		logger.debug("Deleting container");
		dialogHandler.getDeletePDFDialog().initAndPrepareBean(container,
				PDFService.getParentOfPDF(getDataLists(), container));
	}

	/**
	 * Opens the edit dialog for the current tree node
	 */
	public void editPDFContainer() {
		if (!((PDFContainer) getSelectedNode().getData()).getRestricted())
			editPDFContainer((PDFContainer) getSelectedNode().getData());
		else
			MessageHandler.sendGrowlMessagesAsResource("growl.pdf.edit.forbidden");
	}

	/**
	 * Opens the edit dialog for the passed pdf
	 * 
	 * @param container
	 */
	public void editPDFContainer(PDFContainer container) {
		logger.debug("Editing container");
		dialogHandler.getEditPDFDialog().initAndPrepareBean(container);
	}

	/**
	 * On dialog return, reload data (delete, edit and updload=
	 * 
	 * @param event
	 */
	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
			update(true);
		}
	}

	/**
	 * Hides the dialog, returning a reload event
	 */
	@Override
	public void hideDialog() {
		super.hideDialog(new ReloadTaskEvent());
	}

	/**
	 * Hies the dialog an returns the selected pdf as an event
	 */
	public void selectAndHide() {
		super.hideDialog(new PDFSelectEvent(getStreamContainer().getDisplayPDF()));
	}

	/**
	 * Return object for selection mode
	 * 
	 * @author andi
	 *
	 */
	@Getter
	@Setter
	@AllArgsConstructor
	public static class PDFSelectEvent implements DialogReturnEvent {
		private PDFContainer container;
	}
}
