package com.patho.main.ui.task;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PrintDocumentRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.PDFService;
import com.patho.main.service.PDFService.PDFReturn;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.util.pdf.LazyPDFReturnHandler;
import com.patho.main.util.pdf.PDFCreator;
import com.patho.main.util.pdf.PDFGenerator;
import com.patho.main.util.pdf.PDFUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
				.findByTypeAndDefault(DocumentType.DIAGNOSIS_REPORT);

		if (!printDocument.isPresent()) {
			logger.error("No diagnosis print template found");
			return task;
		}

		return updateDiagnosisReportNoneBlocking(task, diagnosisRevision, printDocument.get(), returnHandler);
	}

	public Task updateDiagnosisReportNoneBlocking(Task task, DiagnosisRevision diagnosisRevision,
			PrintDocument printDocument, LazyPDFReturnHandler returnHandler) {

		printDocument.initilize(new InitializeToken("task", diagnosisRevision.getTask()),
				new InitializeToken("diagnosisRevisions", Arrays.asList(diagnosisRevision)),
				new InitializeToken("patient", diagnosisRevision.getPatient()), new InitializeToken("address", ""),
				new InitializeToken("subject", ""));

		Optional<PDFContainer> pdf = PDFUtil.getDiagnosisReport(task, diagnosisRevision);

		File outputDirectory = new File(
				PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN + String.valueOf(task.getPatient().getId()));
		PDFContainer container;
		// generating new pdf
		if (!pdf.isPresent()) {
			logger.debug("Creating new PDF for report");
			PDFReturn res = pdfService.createAndAttachPDF(task, DocumentType.DIAGNOSIS_REPORT_COMPLETED,
					diagnosisRevision.getName(), "",
					PDFContainer.MARKER_DIAGNOSIS.replace("$id", String.valueOf(diagnosisRevision.getId())));
			task = (Task) res.getDataList();
			container = res.getContainer();
		} else {
			logger.debug("Updating pdf for report");
			container = pdf.get();
			container.setName(diagnosisRevision.getName());
			task = taskRepository.save(task);
		}

		logger.debug("Stargin PDF Generation in new Thread");
		new PDFCreator()
		generator.getPDFNoneBlocking(printDocument, outputDirectory, container, true, returnHandler);

		return task;
	}

}
