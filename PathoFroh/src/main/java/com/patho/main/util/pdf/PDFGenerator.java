package com.patho.main.util.pdf;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.common.DateFormat;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.transitory.LoadedPDFContainer;
import com.patho.main.repository.MediaRepository;
import com.patho.main.repository.impl.MediaRepositoryImpl;
import com.patho.main.service.PDFService;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.util.helper.FileUtil;
import com.patho.main.util.helper.TimeUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

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

	// absolute
	private File inputFile;
	private File auxFile;
	private File logFile;
	private File errorDirectory;

	// relative
	private File generatedOutputFile;
	private File outputFile;
	private File thubnailFile;

	private String generationResult;
	private String errorMessage;

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
			PDFContainer result = preparePDFCreation(toUpdateContainer);
			return generatePDF(result);
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
			PDFContainer result = preparePDFCreation(toUpdateContainer);
			String uuid = UUID.randomUUID().toString();

			taskExecutor.execute(new Thread() {
				public void run() {
					logger.debug("Stargin PDF Generation in new Thread");
					PDFContainer returnPDF = generatePDF(result);
					returnHandler.returnPDFContent(returnPDF, uuid);
					logger.debug("PDF Generation completed, thread ended");
				}
			});

			return uuid;
		} catch (Exception e) {
			return null;
		}
	}

	public PDFContainer preparePDFCreation() {
		return preparePDFCreation(null);
	}

	/**
	 * File name as xxx.pdf
	 * 
	 * @param fileName
	 * @return
	 */
	public PDFContainer preparePDFCreation(PDFContainer pdfContainer) {
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

		if (pdfContainer == null) {

			String outPutName = mediaRepository.getUniqueName(outputDirectory, ".pdf");
			outputFile = new File(outputDirectory, outPutName);
			thubnailFile = new File(outputDirectory, outPutName.replace(".pdf", ".png"));

			logger.debug("PDF with output file " + outputFile.getPath() + " dir " + outputDirectory.getPath());
			pdfContainer = new PDFContainer();
			pdfContainer.setName(printTemplate.getGeneratedFileName());
			// sets the absolute path of the file
			pdfContainer.setPath(outputFile.getPath());
			pdfContainer.setThumbnail(generateThumbnail ? thubnailFile.getPath() : null);

		} else {
			outputFile = new File(pdfContainer.getPath());
			thubnailFile = new File(pdfContainer.getThumbnail());
		}

		// getting uniqueName for the pdf
		String createName = mediaRepository.getUniqueName(outputDirectory, ".tex");
		inputFile = new File(workingDirectory, createName);
		auxFile = new File(auxDirectory, createName.replace(".tex", ".aux"));
		logFile = new File(auxDirectory, createName.replace(".tex", ".log"));
		generatedOutputFile = new File(outputDirectory, createName.replace(".tex", ".pdf"));

		if (!mediaRepository.saveString(printTemplate.getFinalContent(), inputFile)) {
			throw new IllegalArgumentException("Could not save pdf to process to " + inputFile.getAbsolutePath());
		}

		logger.debug("Working directory: " + workingDirectory.getAbsolutePath());
		logger.debug("Aux directory: " + auxDirectory.getAbsolutePath());
		logger.debug("Output directory: " + outputDirectory.getPath());
		logger.debug("Input file: " + inputFile.getAbsolutePath());
		logger.debug("Generated file: " + generatedOutputFile.getPath());

		logger.debug("PDF file: " + outputFile.getPath());

		return pdfContainer;
	}

	private PDFContainer generatePDF(PDFContainer container) {
		long time = System.currentTimeMillis();

		try {
			if (generate()) {
				logger.debug("Creating pdf with path: " + outputFile.getPath() + " thub: "
						+ (generateThumbnail ? thubnailFile.getPath() : ""));
				PDFContainer result = container;

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

	public boolean generate() throws IOException {
		return generate(inputFile, outputFile, workingDirectory, auxDirectory);
	}

	public boolean generate(File inputFile, File outputFile, File workingDirectory, File auxDirectory)
			throws IOException {
		return generate(new File("pdflatex"), inputFile, outputFile, workingDirectory, auxDirectory);
	}

	public boolean generate(File pdflatex, File inputFile, File outputFile, File workingDirectory, File auxDirectory)
			throws IOException {

		errorMessage = null;

		String para1 = pdflatex.getPath();
		String para2 = "--interaction=nonstopmode";
		String para3 = "--output-directory=" + mediaRepository.getWriteFile(outputDirectory).getAbsolutePath();
		String para4 = "--aux-directory=" + auxDirectory.getAbsolutePath();
		String para5 = inputFile.getAbsolutePath();

		ProcessBuilder localProcessBuilder = new ProcessBuilder(new String[] { para1, para2, para3, para4, para5 });
		localProcessBuilder.redirectErrorStream(true);
		localProcessBuilder.directory(workingDirectory);

		for (int i = 1; i <= 3; i++) {
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

			generationResult = localStringBuilder.toString();

			try {
				int j = localProcess.waitFor();

				if (j != 0 || !verifyGeneration()) {
					errorMessage = ("Errors occurred while executing pdfLaTeX! Exit value of the process: " + j);
				}

			} catch (InterruptedException localInterruptedException) {
				errorMessage = ("The process pdfLaTeX was interrupted and an exception occurred!" + newline
						+ localInterruptedException.toString());
			}

		}

		if (generateThumbnail) {
			pdfService.createThumbnail(thubnailFile, outputFile, 0, pathoConfig.getFileSettings().getThumbnailDPI());
			logger.debug("Thumbail created: " + thubnailFile.getPath());
		}

		if (errorMessage != null) {
			cleanUp(true, pathoConfig.getFileSettings().isKeepErrorFiles());
			return false;
		} else {
			cleanUp(false, pathoConfig.getFileSettings().isKeepErrorFiles());
			return true;
		}

	}

	public boolean verifyGeneration() {
		boolean result = true;

		if (!inputFile.isFile()) {
			result = false;
			logger.error("Input file not Found! " + inputFile.getAbsolutePath());
		}

		if (!auxFile.isFile()) {
			result = false;
			logger.error("aux file not Found! " + auxFile.getAbsolutePath());
		}

		if (!logFile.isFile()) {
			result = false;
			logger.error("log file not Found! " + logFile.getAbsolutePath());
		}

		if (!mediaRepository.getWriteFile(generatedOutputFile).isFile()) {
			result = false;
			logger.error("out file not Found! " + outputFile.getAbsolutePath());
		} else {

			if (mediaRepository.getWriteFile(outputFile).isFile()) {
				logger.debug("Old file found removing " + outputFile.getPath());
				mediaRepository.delete(outputFile);
			}

			// moving file to correct position
			try {
				FileUtils.moveFile(mediaRepository.getWriteFile(generatedOutputFile),
						mediaRepository.getWriteFile(outputFile));
			} catch (IOException e) {
				e.printStackTrace();
				result = false;
			}
			logger.debug("Moving file to correct destinaion");
		}

		return result;
	}

	/**
	 * Deleting aux files
	 */
	public void cleanUp(boolean error, boolean moveOnError) {
		try {
			if (!error) {
				logger.debug("Deleting aux files");
				mediaRepository.delete(inputFile);
				mediaRepository.delete(auxFile);
				mediaRepository.delete(logFile);
			} else {
				logger.debug("Cleaning up after error");
				if (!moveOnError) {
					if (mediaRepository.getWriteFile(generatedOutputFile).isFile())
						mediaRepository.delete(generatedOutputFile);
					if (mediaRepository.getWriteFile(outputFile).isFile())
						mediaRepository.delete(outputFile);
					mediaRepository.delete(inputFile);
					mediaRepository.delete(auxFile);
					mediaRepository.delete(logFile);

				} else {
					logger.debug("Moving files to error directory");
					FileUtils.moveFileToDirectory(auxFile, errorDirectory, false);
					FileUtils.moveFileToDirectory(inputFile, errorDirectory, false);
					FileUtils.moveFileToDirectory(logFile, errorDirectory, false);
					if (mediaRepository.getWriteFile(generatedOutputFile).isFile())
						FileUtils.moveFileToDirectory(generatedOutputFile, errorDirectory, false);
					if (mediaRepository.getWriteFile(outputFile).isFile())
						FileUtils.moveFileToDirectory(outputFile, errorDirectory, false);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

}
