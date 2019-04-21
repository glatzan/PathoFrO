package com.patho.main.ui.task;

import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.PDFService;
import com.patho.main.service.PDFService.PDFInfo;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.template.DocumentToken;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.pdf.LazyPDFReturnHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Optional;

@Configurable
@Getter
@Setter
public class DiagnosisReportUpdater {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private MediaRepository mediaRepository;

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
    private TaskRepository taskRepository;

    public Task updateDiagnosisReportNoneBlocking(Task task, DiagnosisRevision diagnosisRevision) {
        return updateDiagnosisReportNoneBlocking(task, diagnosisRevision, new LazyPDFReturnHandler() {
            @Override
            public void returnPDFContent(PDFContainer container, String uuid) {
            }
        });
    }

    public Task updateDiagnosisReportNoneBlocking(Task task, DiagnosisRevision diagnosisRevision,
                                                  LazyPDFReturnHandler returnHandler) {
        Optional<PrintDocument> printDocument = printDocumentRepository
                .findByTypeAndDefault(PrintDocumentType.DIAGNOSIS_REPORT);

        if (!printDocument.isPresent()) {
            logger.error("No diagnosis print template found");
            return task;
        }

        return updateDiagnosisReportNoneBlocking(task, diagnosisRevision, printDocument.get(), returnHandler);
    }

    public Task updateDiagnosisReportNoneBlocking(Task task, DiagnosisRevision diagnosisRevision,
                                                  PrintDocument printDocument, LazyPDFReturnHandler returnHandler) {

        printDocument.initialize(new DocumentToken("task", diagnosisRevision.getTask()),
                new DocumentToken("diagnosisRevisions", Arrays.asList(diagnosisRevision)),
                new DocumentToken("patient", diagnosisRevision.getPatient()), new DocumentToken("address", ""),
                new DocumentToken("subject", ""));

        Optional<PDFContainer> pdf = PDFService.findDiagnosisReport(task, diagnosisRevision);

        // generating new pdf
        if (!pdf.isPresent()) {
            logger.debug("Creating new PDF for report");
            PDFReturn res;
            try {
                res = pdfService.createAndAttachPDF(
                        task, printDocument, new PDFInfo(diagnosisRevision.getName(),
                                PrintDocumentType.DIAGNOSIS_REPORT_COMPLETED, "", PDFContainer.MARKER_DIAGNOSIS
                                .replace("$id", String.valueOf(diagnosisRevision.getId()))),
                        true, true, returnHandler);
                task = (Task) res.getDataList();
            } catch (FileNotFoundException e) {
                // TODO Handle error
                e.printStackTrace();
            }
        } else {
            logger.debug("Updating pdf for report");
            PDFContainer container = pdf.get();
            container.setName(diagnosisRevision.getName());
            task = taskRepository.save(task);
            pdfService.updateAttachedPDF(container, printDocument, true, true);
        }

        return task;
    }

}
