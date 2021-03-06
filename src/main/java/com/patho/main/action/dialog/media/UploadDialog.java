package com.patho.main.action.dialog.media;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Patient;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialog.event.ReloadEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.faces.application.FacesMessage;
import java.io.FileNotFoundException;
import java.util.List;

@Getter
@Setter
public class UploadDialog extends AbstractDialog {

    /**
     * Description of the uploaded file
     */
    private String uploadedFileCommentary;

    /**
     * Datalists to media pfds to
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
    private PrintDocumentType fileType;

    /**
     * Type of uploaded file
     */
    private PrintDocumentType[] availableFileTypes;

    /**
     * Associated Patient with datalists
     */
    private Patient patient;

    public UploadDialog initAndPrepareBean(List<DataList> dataList, Patient patient) {
        DataList[] res = new DataList[dataList.size()];
        return initAndPrepareBean(dataList.toArray(res), patient);
    }

    public UploadDialog initAndPrepareBean(DataList[] dataList, Patient patient) {
        if (initBean(dataList, patient, PrintDocumentType.values(), PrintDocumentType.OTHER))
            prepareDialog();
        return this;
    }

    public UploadDialog initAndPrepareBean(DataList[] dataList, Patient patient, PrintDocumentType[] availableFileTypes,
                                           PrintDocumentType selectedFileType) {
        if (initBean(dataList, patient, availableFileTypes, selectedFileType))
            prepareDialog();
        return this;
    }

    public boolean initBean(DataList[] dataList, Patient patient, PrintDocumentType[] availableFileTypes,
                            PrintDocumentType selectedFileType) {

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
    public void initializeUploadFileTypes() {
        setAvailableFileTypes(new PrintDocumentType[]{PrintDocumentType.BIOBANK_INFORMED_CONSENT, PrintDocumentType.COUNCIL_REPLY,
                PrintDocumentType.OTHER, PrintDocumentType.U_REPORT});
    }

    /**
     * Hadels viewl media
     *
     * @param event
     */
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        try {
            logger.debug("Uploadgin to Patient: " + patient.getId());

            PDFReturn res = SpringContextBridge.services().getPdfService().createPDFAndAddToDataList(selectedDatalist.getDataList(), file.getContents(), true, file.getFileName(), getFileType(), "", "", selectedDatalist.getDataList().getFileRepositoryBase().getPath());

            getSelectedDatalist().setDataList(res.getDataList());
            MessageHandler.sendGrowlMessagesAsResource("growl.upload.success.headline", "growl.upload.success.text", res.getContainer().getName());
        } catch (IllegalAccessError e) {
            MessageHandler.sendGrowlMessagesAsResource("growl.upload.failed.headline", "growl.upload.failed.text", FacesMessage.SEVERITY_ERROR);
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
