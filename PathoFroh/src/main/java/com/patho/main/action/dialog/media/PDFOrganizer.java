package com.patho.main.action.dialog.media;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.dialog.DialogHandler;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.BioBank;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.BioBankRepository;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.PDFService;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PDFOrganizer extends AbstractDialog<PDFOrganizer> {

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

	private PDFContainer displayPDF;

	private PDFContainer tooltip;

	private List<DataList> dataLists;

	public PDFOrganizer initAndPrepareBean(Patient patient) {
		if (initBean(patient))
			prepareDialog();
		return this;
	}

	public boolean initBean(Patient patient) {
		setPatient(patient);

		setSelectedNode(null);
		setDisplayPDF(null);
		setDataLists(new ArrayList<DataList>());
		setRoot(null);

		update(true);

		super.initBean(null, Dialog.PDF_ORGANIZER);

		return true;
	}

	/**
	 * Updates the tree menu und reloads the patient's data
	 * 
	 * @param reloadPatient
	 */
	private void update(boolean reloadPatient) {
		if (reloadPatient)
			setPatient(patientRepository.findOptionalById(getPatient().getId(), true).get());

		setRoot(generateTree(patient));

		// unloading pdf is it was selected
		if (displayPDF != null && PDFService.getParentOfPDF(getDataLists(), displayPDF) == null) {
			setSelectedNode(null);
			setDisplayPDF(null);
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

			Optional<BioBank> b = bankRepository.findOptionalByIdAndInitialize(task.getId(), true, true);

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
		this.displayPDF = getSelectedNode() != null ? (PDFContainer) getSelectedNode().getData() : null;
	}

	/**
	 * Returns the thumbnail als stream
	 * 
	 * @return
	 */
	public StreamedContent getThumbnail() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || tooltip == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {

			byte[] img;

			if (mediaRepository.isFile(tooltip.getThumbnail()))
				img = mediaRepository.getBytes(tooltip.getThumbnail());
			else
				img = mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_IMG);

			return new DefaultStreamedContent(new ByteArrayInputStream(img), "image/png");
		}
	}

	/**
	 * Returns the pdf als stream
	 * 
	 * @return
	 */
	public StreamedContent getPDF() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || getDisplayPDF() == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {
			byte[] file;

			if (mediaRepository.isFile(getDisplayPDF().getPath()))
				file = mediaRepository.getBytes(getDisplayPDF().getPath());
			else
				file = mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF);

			return new DefaultStreamedContent(new ByteArrayInputStream(file), "application/pdf",
					getDisplayPDF().getName());
		}
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
				|| ((PDFContainer) dragNode.getData()).isRestricted()) {
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
			logger.debug("Uploadgin to Patient: " + patient.getId());
			PDFReturn res = pdfService.createAndAttachPDF(patient, file, DocumentType.OTHER, "", "", true,
					new File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN + String.valueOf(patient.getId())));

			setPatient((Patient) res.getDataList());
			MessageHandler.sendGrowlMessagesAsResource("growl.upload.success");
		} catch (IllegalAccessError e) {
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
		MessageHandler.sendGrowlMessagesAsResource("growl.print",
				success ? "growl.print.success" : "growl.print.failed",
				success ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR);
	}

	/**
	 * Deletes the pdf of the current tree node
	 */
	public void deletePDFContainer() {
		if (!((PDFContainer) getSelectedNode().getData()).isRestricted())
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
		if (!((PDFContainer) getSelectedNode().getData()).isRestricted())
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
		super.hideDialog(new ReloadEvent());
	}

}
