package com.patho.main.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.hibernate.Hibernate;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.config.PathoConfig;
import com.patho.main.model.BioBank;
import com.patho.main.model.Council;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.BioBankRepository;
import com.patho.main.repository.CouncilRepository;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.PDFRepository;
import com.patho.main.repository.PatientRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.template.PrintDocument.DocumentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class PDFService extends AbstractService {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private BioBankRepository bioBankRepository;

	@Autowired
	private MediaRepository mediaRepository;

	@Autowired
	private PDFRepository pdfRepository;

	@Autowired
	private CouncilRepository councilRepository;

	@Autowired
	private PathoConfig pathoConfig;

	public DataList attachPDF(DataList dataList, PDFContainer pdfContainer) {
		dataList.getAttachedPdfs().add(pdfContainer);

		if (dataList instanceof Patient) {
			return patientRepository.save((Patient) dataList,
					resourceBundle.get("log.pdf.uploaded", pdfContainer.getName()));
		} else if (dataList instanceof Task) {
			return taskRepository.save((Task) dataList, resourceBundle.get("log.pdf.uploaded", pdfContainer.getName()));
		} else if (dataList instanceof BioBank) {
			return bioBankRepository.save((BioBank) dataList,
					resourceBundle.get("log.pdf.uploaded", pdfContainer.getName()));
		} else if (dataList instanceof Council) {
			return councilRepository.save((Council) dataList,
					resourceBundle.get("log.pdf.uploaded", pdfContainer.getName()));
		} else {
			throw new IllegalArgumentException("List type not supported (" + dataList + ")");
		}
	}

	public PDFReturn createAndAttachPDF(DataList dataList, DocumentType type, String name, String commentary,
			String commentaryIntern, File folder) {
		return createAndAttachPDF(dataList, type, name, commentary, commentaryIntern, null, false, folder);
	}

	public PDFReturn createAndAttachPDF(DataList dataList, UploadedFile file, DocumentType type, String commentary,
			String commentaryIntern, boolean createThumbnail, File folder) {
		return createAndAttachPDF(dataList, type, file.getFileName(), commentary, commentaryIntern, file.getContents(),
				createThumbnail, folder);
	}

	public PDFReturn createAndAttachPDF(DataList dataLists, DocumentType type, String name, String commentary,
			String commentaryIntern, byte[] content, boolean createThumbnail, File folder) {

		PDFContainer uploadedPDF = new PDFContainer();
		uploadedPDF.setName(name);
		uploadedPDF.setType(type);
		uploadedPDF.setCommentary(commentary);
		uploadedPDF.setIntern(commentaryIntern);

		try {
			File uniqueFile = new File(folder, mediaRepository.getUniqueName(folder, ".pdf"));
			uploadedPDF.setPath(uniqueFile.getPath());
			uploadedPDF.setThumbnail(uniqueFile.getPath().replace(".pdf", ".png"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalAccessError("Could not updaload data");
		}

		return createAndAttachPDF(dataLists, uploadedPDF, content, createThumbnail);
	}

	public PDFReturn createAndAttachPDF(DataList dataList, PDFContainer pdfContainer, byte[] pdfContent,
			boolean createThumbnail) {

		pdfContainer = pdfRepository.save(pdfContainer);

		dataList = attachPDF(dataList, pdfContainer);

		try {
			if (pdfContent != null) {
				logger.debug("Saving filed to disk");
				mediaRepository.saveBytes(pdfContent, pdfContainer.getPath());
				if (createThumbnail)
					mediaRepository.saveImage(
							gerateThumbnail(pdfContent, 0, pathoConfig.getFileSettings().getThumbnailDPI()),
							pdfContainer.getThumbnail());
			} else {
				logger.debug("Virtual PDF no content!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Error while saving file, removing file from datalists");
			dataList = deletePdf(dataList, pdfContainer);
			throw new IllegalAccessError("Could not updaload data");
		}

		return new PDFReturn(dataList, pdfContainer);
	}

	public DataList removePdf(DataList dataList, PDFContainer pdfContainer) {
		dataList.removeReport(pdfContainer);
		return saveDataList(dataList, resourceBundle.get("log.pdf.removed", pdfContainer.getName()));
	}

	public DataList deletePdf(DataList dataList, PDFContainer pdfContainer) {

		dataList = removePdf(dataList, pdfContainer);

		if (pdfContainer.getPath() != null)
			mediaRepository.delete(pdfContainer.getPath());
		if (pdfContainer.getThumbnail() != null)
			mediaRepository.delete(pdfContainer.getThumbnail());

		pdfRepository.delete(pdfContainer);

		return dataList;
	}

	public void movePdf(DataList from, DataList to, PDFContainer pdfContainer) {
		removePdf(from, pdfContainer);
		attachPDF(to, pdfContainer);
	}

	public void movePdf(List<DataList> from, DataList to, PDFContainer pdfContainer) {
		for (DataList dataList : from) {
			if (dataList.containsReport(pdfContainer)) {
				if (dataList.equals(to))
					return;
				dataList = removePdf(dataList, pdfContainer);
				break;
			}
		}

		attachPDF(to, pdfContainer);
	}

	public void copyPdf() {

	}

	public DataList saveDataList(DataList dataList, String log) {
		if (dataList instanceof Patient) {
			return patientRepository.save((Patient) dataList, log);
		} else if (dataList instanceof Task) {
			return taskRepository.save((Task) dataList, log);
		} else if (dataList instanceof BioBank) {
			return bioBankRepository.save((BioBank) dataList, log);
		} else {
			throw new IllegalArgumentException("List type not supported (" + dataList + ")");
		}
	}

	public Patient initializeDataListTree(Patient patient) {
		patient = patientRepository.save(patient);

		Hibernate.initialize(patient.getAttachedPdfs());
		for (Task task : patient.getTasks()) {
			Hibernate.initialize(task.getAttachedPdfs());
			Hibernate.initialize(task.getCouncils());

			for (Council council : task.getCouncils()) {
				Hibernate.initialize(council.getAttachedPdfs());
			}
		}

		return patient;
	}

	public File createThumbnail(File output, File input, int pageNo, int dpi) {
		byte[] load = mediaRepository.getBytes(input);
		return createThumbnail(output, load, pageNo, dpi);
	}

	public File createThumbnail(File output, byte[] pdf, int pageNo, int dpi) {
		mediaRepository.saveImage(gerateThumbnail(pdf, 0, pathoConfig.getFileSettings().getThumbnailDPI()), output);
		return output;
	}

	public BufferedImage gerateThumbnail(File path, int pageNo, int dpi) {
		return gerateThumbnail(mediaRepository.getBytes(path), pageNo, dpi);
	}

	/**
	 * Returns a buffered Image from a pdf page, page number is zero based
	 * 
	 * @param pdf
	 * @param page
	 * @param path
	 * @return
	 */
	public BufferedImage gerateThumbnail(byte[] pdf, int pageNo, int dpi) {
		PDDocument document = null;
		try {
			document = PDDocument.load(pdf);

			PDFRenderer pdfRenderer = new PDFRenderer(document);

			if (pageNo < document.getNumberOfPages())
				return pdfRenderer.renderImageWithDPI(pageNo, dpi, ImageType.RGB);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (document != null)
				try {
					document.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	public List<DataList> getDataListsOfPatient(Patient p) {
		List<DataList> result = new ArrayList<DataList>();

		result.add(p);

		for (Task task : p.getTasks()) {
			result.add(task);

			Optional<BioBank> bio = bioBankRepository.findOptionalByTask(task);

			if (bio.isPresent())
				result.add(bio.get());

			for (Council c : task.getCouncils()) {
				result.add(c);
			}
		}

		return result;
	}

	public static DataList getParentOfPDF(List<DataList> dataLists, PDFContainer container) {

		for (DataList dataList : dataLists) {
			for (PDFContainer pContainer : dataList.getAttachedPdfs()) {
				if (pContainer.equals(container))
					return dataList;
			}
		}

		return null;
	}

	/**
	 * Searches for a datalist within all datalists e.g. of a patient. This is to
	 * find the datalist with the current version.
	 * 
	 * @param dataLists
	 * @param findeThisList
	 * @return
	 */
	public static DataList getDatalistFromDatalists(List<DataList> dataLists, DataList findeThisList) {
		for (DataList dataList : dataLists) {
			if (dataList.getId() == findeThisList.getId() && dataList.getClass().equals(findeThisList.getClass()))
				return dataList;
		}

		return null;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class PDFReturn {
		DataList dataList;
		PDFContainer container;
	}
}
