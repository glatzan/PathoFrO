package com.patho.main.action.dialog.media;

import java.io.File;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Patient;
import com.patho.main.service.PDFService;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class UploadDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	/**
	 * Description of the uploaded file
	 */
	private String uploadedFileCommentary;

	/**
	 * Datalists to upload pfds to
	 */
	private DataListContainer[] dataLists;

	/**
	 * Transformer for datalists
	 */
	private DefaultTransformer<DataListContainer> dataListTransformer;

	/**
	 * Selected Datalist
	 */
	private DataListContainer selectedDatalist;

	/**
	 * Type of uploaded file
	 */
	private DocumentType fileType;

	/**
	 * Type of uploaded file
	 */
	private DocumentType[] availableFileTypes;

	/**
	 * Associated Patient with datalists
	 */
	private Patient patient;

	public UploadDialog initAndPrepareBean(List<DataList> dataList, Patient patient) {
		DataList[] res = new DataList[dataList.size()];
		return initAndPrepareBean(dataList.toArray(res), patient);
	}

	public UploadDialog initAndPrepareBean(DataList[] dataList, Patient patient) {
		if (initBean(dataList, patient, DocumentType.values(), DocumentType.OTHER))
			prepareDialog();
		return this;
	}

	public UploadDialog initAndPrepareBean(DataList[] dataList, Patient patient, DocumentType[] availableFileTypes,
			DocumentType selectedFileType) {
		if (initBean(dataList, patient, availableFileTypes, selectedFileType))
			prepareDialog();
		return this;
	}

	public boolean initBean(DataList[] dataList, Patient patient, DocumentType[] availableFileTypes,
			DocumentType selectedFileType) {

		DataListContainer[] containers = new DataListContainer[dataList.length];

		for (int i = 0; i < dataList.length; i++) {
			containers[i] = new DataListContainer(i, dataList[i]);
		}

		setPatient(patient);
		setDataLists(containers);
		setSelectedDatalist(getDataLists()[0]);
		setDataListTransformer(new DefaultTransformer<DataListContainer>(containers));
		setUploadedFileCommentary("");
		setAvailableFileTypes(availableFileTypes);
		setFileType(fileType);
		setFileType(selectedFileType);

		super.initBean(null, Dialog.PDF_UPLOAD);

		return true;
	}

	/**
	 * Sets file types for uploading
	 */
	public void initilizeUploadFileTypes() {
		setAvailableFileTypes(new DocumentType[] { DocumentType.BIOBANK_INFORMED_CONSENT, DocumentType.COUNCIL_REPLY,
				DocumentType.OTHER, DocumentType.U_REPORT });
	}

	/**
	 * Hadels viewl upload
	 * 
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		try {
			logger.debug("Uploadgin to Patient: " + patient.getId());

			PDFReturn res = pdfService.createAndAttachPDF(selectedDatalist.getDataList(), file, getFileType(),
					uploadedFileCommentary, "", true, patient);

			getSelectedDatalist().setDataList(res.getDataList());
			MessageHandler.sendGrowlMessagesAsResource("growl.upload.success");
		} catch (IllegalAccessError e) {
			MessageHandler.sendGrowlMessagesAsResource("growl.upload.failed", FacesMessage.SEVERITY_ERROR);
		}
	}

	/**
	 * Hides the dialog with a relaod event
	 */
	@Override
	public void hideDialog() {
		super.hideDialog(new ReloadEvent());
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class DataListContainer implements ID {
		private long id;
		private DataList dataList;
	}
}
