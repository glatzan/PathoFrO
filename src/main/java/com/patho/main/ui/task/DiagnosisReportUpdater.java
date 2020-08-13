package com.patho.main.ui.task;

import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.service.PDFService;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.template.DocumentToken;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.pdf.LazyPDFReturnHandler;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

@Getter
@Setter
public class DiagnosisReportUpdater {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Task updateDiagnosisReportNoneBlocking(Task task, DiagnosisRevision diagnosisRevision) {
        return updateDiagnosisReportNoneBlocking(task, diagnosisRevision, new LazyPDFReturnHandler() {
            @Override
            public void returnPDFContent(PDFContainer container, String uuid) {
            }
        });
    }

    public Task updateDiagnosisReportNoneBlocking(Task task, DiagnosisRevision diagnosisRevision,
                                                  LazyPDFReturnHandler returnHandler) {
        Optional<PrintDocument> printDocument = SpringContextBridge.services().getPrintDocumentRepository()
                .findByTypeAndDefault(PrintDocumentType.DIAGNOSIS_REPORT);

        if (!printDocument.isPresent()) {
            logger.error("No reportIntent print template found");
            return task;
        }

        return updateDiagnosisReportNoneBlocking(task, diagnosisRevision, printDocument.get(), returnHandler);
    }

    public Task updateDiagnosisReportNoneBlocking(Task task, DiagnosisRevision diagnosisRevision,
                                                  PrintDocument printDocument, LazyPDFReturnHandler returnHandler) {
        Task t = SpringContextBridge.services().getTaskRepository().findByID(diagnosisRevision.getTask(), false, true, true, false, false);

        printDocument.initialize(new DocumentToken("task", t),
                new DocumentToken("diagnosisRevisions", Arrays.asList(diagnosisRevision)),
                new DocumentToken("patient", diagnosisRevision.getPatient()), new DocumentToken("address", ""),
                new DocumentToken("subject", ""));

        PDFContainer pdf = PDFService.findDiagnosisReport(task, diagnosisRevision);

        // generating new pdf
        if (pdf == null) {
            logger.debug("Creating new PDF for report");
            PDFReturn res;
            res = SpringContextBridge.services().getPdfService().createPDFAndAddToDataList(
                    task, printDocument, true, diagnosisRevision.getName(), PrintDocumentType.DIAGNOSIS_REPORT_COMPLETED, "", PDFContainer.MARKER_DIAGNOSIS.replace("$id", String.valueOf(diagnosisRevision.getId())),
                    task.getFileRepositoryBase().getPath(), true, returnHandler);

            task = (Task) res.getDataList();
        } else {
            logger.debug("Updating pdf for report");
            pdf.setName(diagnosisRevision.getName());
            task = SpringContextBridge.services().getTaskRepository().save(task);
            SpringContextBridge.services().getPdfService().updatePDF(pdf, printDocument, true, true, null);
        }

        return task;
    }

}
