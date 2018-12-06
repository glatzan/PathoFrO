package com.patho.main.util.pdf;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;
import com.patho.main.service.PDFService;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.util.printer.LoadedPDFContainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class PDFGenerator {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static String newline = System.getProperty("line.separator");

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	private PrintDocument printTemplate;
	private File outputDirectoryPath;

	// absolute
	private File workingDirectory;
	private File auxDirectory;
	// relative
	private File outputDirectory;

	private File errorDirectory;

	private boolean generateThumbnail;

	public PDFContainer getPDF(PrintDocument template, File outputDirectory) {
		return getPDF(template, outputDirectory, false);
	}

	public PDFContainer getPDF(PrintDocument template, File outputDirectory, boolean generateThumbnail) {
		return getPDF(template, outputDirectory, null, generateThumbnail);
	}

	public PDFContainer getPDF(PrintDocument template, File outputDirectory, PDFContainer toUpdateContainer,
			boolean generateThumbnail) {
		this.printTemplate = template;
		this.generateThumbnail = generateThumbnail;
		this.outputDirectory = outputDirectory;

		try {
			PDFDataAndPathContainer pdfDataAndPathContainer = preparePDFCreation(toUpdateContainer);
			return generatePDF(pdfDataAndPathContainer);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public String getPDFNoneBlocking(PrintDocument template, File outputDirectory, LazyPDFReturnHandler returnHandler) {
		return getPDFNoneBlocking(template, outputDirectory, null, false, returnHandler);
	}

	public String getPDFNoneBlocking(PrintDocument template, File outputDirectory, PDFContainer toUpdateContainer,
			boolean generateThumbnail, LazyPDFReturnHandler returnHandler) {
		this.printTemplate = template;
		this.generateThumbnail = generateThumbnail;
		this.outputDirectory = outputDirectory;

		try {
			PDFDataAndPathContainer pdfDataAndPathContainer = preparePDFCreation(toUpdateContainer);
			String uuid = UUID.randomUUID().toString();

			taskExecutor.execute(new Thread() {
				public void run() {
					logger.debug("Stargin PDF Generation in new Thread");
					PDFContainer returnPDF = generatePDF(pdfDataAndPathContainer);
					returnHandler.returnPDFContent(returnPDF, uuid);
					logger.debug("PDF Generation completed, thread ended");
				}
			});

			return uuid;
		} catch (Exception e) {
			return null;
		}
	}

	public PDFDataAndPathContainer preparePDFCreation() {
		return preparePDFCreation(null);
	}

	/**
	 * File name as xxx.pdf
	 * 
	 * @param fileName
	 * @return
	 */
	public PDFDataAndPathContainer preparePDFCreation(PDFContainer pdfContainer) {
		workingDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getWorkDirectory());

		// checking directories
		if (!workingDirectory.isDirectory() && !workingDirectory.mkdirs()) {
			logger.error("Error directory not found: work directory {}", workingDirectory.getAbsolutePath());
			throw new IllegalArgumentException(
					"Error directory not found: work directory " + workingDirectory.getAbsolutePath());
		}

		auxDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getAuxDirectory());

		if (!auxDirectory.isDirectory() && !auxDirectory.mkdirs()) {
			logger.error("Error directory not found: aux directory {}", auxDirectory.getAbsolutePath());
			throw new IllegalArgumentException(
					"Error directory not found: aux directory " + auxDirectory.getAbsolutePath());
		}

		if (!mediaRepository.getWriteFile(outputDirectory).isDirectory()
				&& !mediaRepository.getWriteFile(outputDirectory).mkdirs()) {
			logger.error("Error directory not found: output directory {}",
					mediaRepository.getWriteFile(outputDirectory).getAbsolutePath());
			throw new IllegalArgumentException("Error directory not found: output directory "
					+ mediaRepository.getWriteFile(outputDirectory).getAbsolutePath());
		}

		errorDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getErrorDirectory());

		if (!errorDirectory.isDirectory() && !errorDirectory.mkdirs()) {
			logger.error("Error directory not found: error directory {}", errorDirectory.getAbsolutePath());
			throw new IllegalArgumentException(
					"Error directory not found: error directory " + errorDirectory.getAbsolutePath());
		}

		PDFDataAndPathContainer result = new PDFDataAndPathContainer();

		if (pdfContainer == null) {

			String outPutName = mediaRepository.getUniqueName(outputDirectory, ".pdf");
			result.outputFile = new File(outputDirectory, outPutName);
			result.thubnailFile = new File(outputDirectory, outPutName.replace(".pdf", ".png"));

			logger.debug("PDF with output file " + result.outputFile.getPath() + " dir " + outputDirectory.getPath());
			pdfContainer = new PDFContainer();
			pdfContainer.setName(printTemplate.getGeneratedFileName());
			pdfContainer.setType(printTemplate.getDocumentType());
			// sets the absolute path of the file
			pdfContainer.setPath(result.outputFile.getPath());
			pdfContainer.setThumbnail(generateThumbnail ? result.thubnailFile.getPath() : null);

		} else {
			result.outputFile = new File(pdfContainer.getPath());
			result.thubnailFile = new File(pdfContainer.getThumbnail());
		}

		result.container = pdfContainer;

		// getting uniqueName for the pdf
		String createName = mediaRepository.getUniqueName(outputDirectory, ".tex");
		result.inputFile = new File(workingDirectory, createName);
		result.auxFile = new File(auxDirectory, createName.replace(".tex", ".aux"));
		result.logFile = new File(auxDirectory, createName.replace(".tex", ".log"));
		result.generatedOutputFile = new File(outputDirectory, createName.replace(".tex", ".pdf"));

		if (!mediaRepository.saveString(printTemplate.getFinalContent(), result.inputFile)) {
			throw new IllegalArgumentException(
					"Could not save pdf to process to " + result.inputFile.getAbsolutePath());
		}

		logger.debug("Working directory: " + workingDirectory.getAbsolutePath());
		logger.debug("Aux directory: " + auxDirectory.getAbsolutePath());
		logger.debug("Output directory: " + outputDirectory.getPath());
		logger.debug("Input file: " + result.inputFile.getAbsolutePath());
		logger.debug("Generated file: " + result.generatedOutputFile.getPath());

		logger.debug("PDF file: " + result.outputFile.getPath());

		return result;
	}

	private PDFContainer generatePDF(PDFDataAndPathContainer pdfDataAndPathContainer) {
		long time = System.currentTimeMillis();

		try {
			if (generate(pdfDataAndPathContainer)) {
				logger.debug("Creating pdf with path: " + pdfDataAndPathContainer.outputFile.getPath() + " thub: "
						+ (generateThumbnail ? pdfDataAndPathContainer.thubnailFile.getPath() : ""));
				PDFContainer result = pdfDataAndPathContainer.container;

				if (printTemplate.isAfterPDFCreationHook())
					result = printTemplate.onAfterPDFCreation(result);

				logger.debug("PDF generation in " + (time / 1000) + " sec");

				return result;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean generate(PDFDataAndPathContainer pdfDataAndPathContainer) throws IOException {
		return generate(pdfDataAndPathContainer, workingDirectory, auxDirectory);
	}

	public boolean generate(PDFDataAndPathContainer pdfDataAndPathContainer, File workingDirectory, File auxDirectory)
			throws IOException {
		return generate(new File("pdflatex"), pdfDataAndPathContainer, workingDirectory, auxDirectory);
	}

	public boolean generate(File pdflatex, PDFDataAndPathContainer pdfDataAndPathContainer, File workingDirectory,
			File auxDirectory) throws IOException {

		pdfDataAndPathContainer.errorMessage = null;

		String para1 = pdflatex.getPath();
		String para2 = "--interaction=nonstopmode";
		String para3 = "--output-directory=" + mediaRepository.getWriteFile(outputDirectory).getAbsolutePath();
		String para4 = "--aux-directory=" + auxDirectory.getAbsolutePath();
		String para5 = pdfDataAndPathContainer.inputFile.getAbsolutePath();

		ProcessBuilder localProcessBuilder = new ProcessBuilder(new String[] { para1, para2, para3, para4, para5 });
		localProcessBuilder.redirectErrorStream(true);
		localProcessBuilder.directory(workingDirectory);

		int loops = 2;

		for (int i = 1; i <= loops; i++) {
			Process localProcess = localProcessBuilder.start();

			InputStreamReader localInputStreamReader = new InputStreamReader(localProcess.getInputStream());
			BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader);
			StringBuilder localStringBuilder = new StringBuilder();

			String res = null;
			try {
				while ((res = localBufferedReader.readLine()) != null) {
					localStringBuilder.append(res + newline);
				}
			} finally {
				localBufferedReader.close();
			}

			pdfDataAndPathContainer.generationResult = localStringBuilder.toString();

			try {
				int j = localProcess.waitFor();

				if (j != 0 || !verifyGeneration(pdfDataAndPathContainer, i == loops)) {
					pdfDataAndPathContainer.errorMessage = ("Errors occurred while executing pdfLaTeX! Exit value of the process: "
							+ j);
				}

			} catch (InterruptedException localInterruptedException) {
				pdfDataAndPathContainer.errorMessage = ("The process pdfLaTeX was interrupted and an exception occurred!"
						+ newline + localInterruptedException.toString());
			}

		}

		if (generateThumbnail) {
			pdfService.createThumbnail(pdfDataAndPathContainer.thubnailFile, pdfDataAndPathContainer.outputFile, 0,
					pathoConfig.getFileSettings().getThumbnailDPI());
			logger.debug("Thumbail created: " + pdfDataAndPathContainer.thubnailFile.getPath());
		}

		if (pdfDataAndPathContainer.errorMessage != null) {
			cleanUp(pdfDataAndPathContainer, true, pathoConfig.getFileSettings().isKeepErrorFiles());
			return false;
		} else {
			cleanUp(pdfDataAndPathContainer, false, pathoConfig.getFileSettings().isKeepErrorFiles());
			return true;
		}

	}

	public boolean verifyGeneration(PDFDataAndPathContainer pdfDataAndPathContainer) {
		return verifyGeneration(pdfDataAndPathContainer, true);
	}

	public boolean verifyGeneration(PDFDataAndPathContainer pdfDataAndPathContainer, boolean move) {
		boolean result = true;

		if (!pdfDataAndPathContainer.inputFile.isFile()) {
			result = false;
			logger.error("Input file not Found! " + pdfDataAndPathContainer.inputFile.getAbsolutePath());
		}

		if (!pdfDataAndPathContainer.auxFile.isFile()) {
			result = false;
			logger.error("aux file not Found! " + pdfDataAndPathContainer.auxFile.getAbsolutePath());
		}

		if (!pdfDataAndPathContainer.logFile.isFile()) {
			result = false;
			logger.error("log file not Found! " + pdfDataAndPathContainer.logFile.getAbsolutePath());
		}

		if (!mediaRepository.getWriteFile(pdfDataAndPathContainer.generatedOutputFile).isFile()) {
			result = false;
			logger.error("out file not Found! " + pdfDataAndPathContainer.outputFile.getAbsolutePath());
		} else {

			if (move) {
				if (mediaRepository.getWriteFile(pdfDataAndPathContainer.outputFile).isFile()) {
					logger.debug("Old file found removing " + pdfDataAndPathContainer.outputFile.getPath());
					mediaRepository.delete(pdfDataAndPathContainer.outputFile);
				}

				// moving file to correct position
				try {
					FileUtils.moveFile(mediaRepository.getWriteFile(pdfDataAndPathContainer.generatedOutputFile),
							mediaRepository.getWriteFile(pdfDataAndPathContainer.outputFile));
				} catch (IOException e) {
					e.printStackTrace();
					result = false;
				}
				logger.debug("Moving file to correct destinaion");
			}
		}

		return result;
	}

	/**
	 * Deleting aux files
	 */
	public void cleanUp(PDFDataAndPathContainer pdfDataAndPathContainer, boolean error, boolean moveOnError) {
		try {
			if (!error) {
				if (pathoConfig.getFileSettings().isCleanup()) {
					logger.debug("Deleting aux files");
					mediaRepository.delete(pdfDataAndPathContainer.inputFile);
					mediaRepository.delete(pdfDataAndPathContainer.auxFile);
					mediaRepository.delete(pdfDataAndPathContainer.logFile);
				} else {
					logger.debug("Clean-up disabled");
					logger.debug("Text file : {}", pdfDataAndPathContainer.inputFile);
				}

			} else {
				logger.debug("Cleaning up after error");
				if (!moveOnError) {
					if (mediaRepository.getWriteFile(pdfDataAndPathContainer.generatedOutputFile).isFile())
						mediaRepository.delete(pdfDataAndPathContainer.generatedOutputFile);
					if (mediaRepository.getWriteFile(pdfDataAndPathContainer.outputFile).isFile())
						mediaRepository.delete(pdfDataAndPathContainer.outputFile);
					mediaRepository.delete(pdfDataAndPathContainer.inputFile);
					mediaRepository.delete(pdfDataAndPathContainer.auxFile);
					mediaRepository.delete(pdfDataAndPathContainer.logFile);

				} else {
					logger.debug("Moving files to error directory");
					FileUtils.moveFileToDirectory(pdfDataAndPathContainer.auxFile, errorDirectory, false);
					FileUtils.moveFileToDirectory(pdfDataAndPathContainer.inputFile, errorDirectory, false);
					FileUtils.moveFileToDirectory(pdfDataAndPathContainer.logFile, errorDirectory, false);
					if (mediaRepository.getWriteFile(pdfDataAndPathContainer.generatedOutputFile).isFile())
						FileUtils.moveFileToDirectory(pdfDataAndPathContainer.generatedOutputFile, errorDirectory,
								false);
					if (mediaRepository.getWriteFile(pdfDataAndPathContainer.outputFile).isFile())
						FileUtils.moveFileToDirectory(pdfDataAndPathContainer.outputFile, errorDirectory, false);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PDFContainer generatePDFContainerForCachedData(PDFContainer container, byte[] data) {
		
	}

	/**
	 * Returns the amount of pdf Pages
	 * 
	 * @param container
	 * @return
	 */
	public static int countPDFPages(LoadedPDFContainer container) {
		PdfReader pdfReader;
		try {
			pdfReader = new PdfReader(container.getPdfData());
			pdfReader.close();
			return pdfReader.getNumberOfPages();

		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}

	}

	/**
	 * Merges a list of pdf into one single pdf
	 * 
	 * @param containers
	 * @param name
	 * @param type
	 * @return
	 */
	public static LoadedPDFContainer mergePdfs(List<LoadedPDFContainer> containers, String name, DocumentType type) {

		try {
			Document document = new Document();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			for (LoadedPDFContainer pdfContainer : containers) {
				PdfReader pdfReader = new PdfReader(pdfContainer.getPdfData());
				for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
					document.newPage();
					// import the page from source pdf
					PdfImportedPage page = writer.getImportedPage(pdfReader, i);
					// add the page to the destination pdf
					cb.addTemplate(page, 0, 0);
				}
			}
			document.close();

			return new LoadedPDFContainer(type, name, out.toByteArray(), null);
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Getter
	@Setter
	public class PDFDataAndPathContainer {
		private PDFContainer container;

		// relative
		private File generatedOutputFile;
		private File outputFile;
		private File thubnailFile;

		// absolute
		private File inputFile;
		private File auxFile;
		private File logFile;

		private String generationResult;
		private String errorMessage;
	}

}
