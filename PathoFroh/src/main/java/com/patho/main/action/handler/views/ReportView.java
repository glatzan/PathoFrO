package com.patho.main.action.handler.views;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.handler.GlobalEditViewHandler;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.model.util.audit.Audit;
import com.patho.main.repository.MediaRepository;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.ui.task.DiagnosisReportUpdater;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.pdf.PDFUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configurable
public class ReportView extends AbstractTaskView {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	/**
	 * List of all diagnosis revisions with correspondending reports
	 */
	public List<DiagnosisReportData> data;

	/**
	 * The data which are displayed
	 */
	private DiagnosisReportData selectedData;

	/**
	 * The selected PDF container
	 */
	private PDFContainer selectedContainer;

	/**
	 * If true the update poll will be started
	 */
	private boolean generatingPDFs;

	/**
	 * If true the update poll will be stoped
	 */
	private boolean generationCompleted;

	/**
	 * True if selected pdf is currently generating
	 */
	private boolean generatingSelectedPDF;

	public ReportView(GlobalEditViewHandler globalEditViewHandler) {
		super(globalEditViewHandler);
	}

	public void loadView() {
		data = new ArrayList<DiagnosisReportData>();

		setGeneratingPDFs(false);
		setSelectedContainer(null);
		setGeneratingSelectedPDF(false);
		setGenerationCompleted(false);

		Task task = getTask();

		for (int i = 0; i < task.getDiagnosisRevisions().size(); i++) {
			DiagnosisRevision revision = HistoUtil.getNElement(task.getDiagnosisRevisions(), i);

			Optional<PDFContainer> c = PDFUtil.getDiagnosisReport(task, revision);

			if (!c.isPresent() && revision.getCompletionDate() != 0) {
				logger.debug("No report present, generating new report");
				task = new DiagnosisReportUpdater().updateDiagnosisReportNoneBlocking(task, revision);
				data.add(new DiagnosisReportData(revision, PDFUtil.getDiagnosisReport(task, revision).get(), true));
				setGeneratingPDFs(true);
			} else if (revision.getCompletionDate() != 0) {
				data.add(new DiagnosisReportData(revision, c.get(), false));
			} else {
				PDFContainer container = new PDFContainer(DocumentType.DIAGNOSIS_REPORT_NOT_APPROVED,
						revision.getName(), PathoConfig.REPORT_NOT_APPROVED_PDF, PathoConfig.REPORT_NOT_APPROVED_IMG);

				container.setAudit(new Audit());
				container.getAudit().setCreatedOn(revision.getCreationDate());

				data.add(new DiagnosisReportData(revision, container, false));
			}

		}

		if (data.size() > 0) {
			setSelectedPDF(data.get(0));
		}

		setTask(task);
	}

	public void updateData() {
		getData().stream().forEach(p -> p.update());

		if (isGeneratingSelectedPDF()) {
			setSelectedPDF(getSelectedData());
		}

		boolean loading = getData().stream().anyMatch(p -> p.isLoading());
		setGenerationCompleted(!loading);
		setGeneratingPDFs(loading);
	}

	/**
	 * Returns the thumbnail als stream
	 * 
	 * @return
	 */
	public StreamedContent getThumbnail() {
		if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
			// So, we're rendering the HTML. Return a stub StreamedContent so that it will
			// generate right URL.
			return new DefaultStreamedContent();
		} else {
			// So, browser is requesting the image. Return a real StreamedContent with the
			// image bytes.
			String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");

			if (id != null && mediaRepository.isFile(id)) {
				byte[] img = mediaRepository.getBytes(id);
				return new DefaultStreamedContent(new ByteArrayInputStream(img), "image/png");
			} else
				return new DefaultStreamedContent(
						new ByteArrayInputStream(mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF)), "image/png");
		}
	}

	public StreamedContent getPDF() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || getSelectedContainer() == null) {
			// So, we're rendering the HTML. Return a stub StreamedContent so
			// that it will generate right URL.
			return new DefaultStreamedContent();
		} else {

			byte[] img;

			if (mediaRepository.isFile(getSelectedContainer().getPath()))
				img = mediaRepository.getBytes(getSelectedContainer().getPath());
			else
				img = mediaRepository.getBytes(PathoConfig.PDF_NOT_FOUND_PDF);

			return new DefaultStreamedContent(new ByteArrayInputStream(img), "application/pdf",
					getSelectedContainer().getName());
		}
	}

	public void setSelectedPDF(DiagnosisReportData data) {
		logger.debug("Setting pdf " + data);
		if (data != null) {
			setSelectedData(data);
			if (data.isLoading()) {
				setGeneratingSelectedPDF(true);
				setSelectedContainer(null);
			} else {
				setGeneratingSelectedPDF(false);
				setSelectedContainer(data.getPdf());
			}
		}
	}

	@Getter
	@Setter
	@Configurable
	public static class DiagnosisReportData {

		protected final Logger logger = LoggerFactory.getLogger(this.getClass());

		@Autowired
		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private MediaRepository mediaRepository;

		private DiagnosisRevision diagnosis;

		private PDFContainer pdf;

		/**
		 * True if the image is generated
		 */
		private boolean loading;

		public DiagnosisReportData(DiagnosisRevision diagnosis, PDFContainer pdf) {
			this(diagnosis, pdf, false);
		}

		public DiagnosisReportData(DiagnosisRevision diagnosis, PDFContainer pdf, boolean loading) {
			super();
			this.diagnosis = diagnosis;
			this.pdf = pdf;
			this.loading = loading;
		}

		public void update() {
			if (loading) {
				loading = !mediaRepository.isFile(pdf.getPath());
			}
		}
	}
}