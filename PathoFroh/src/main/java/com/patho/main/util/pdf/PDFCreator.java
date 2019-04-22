package com.patho.main.util.pdf;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import com.patho.main.config.PathoConfig;
import com.patho.main.util.print.LoadedPrintPDFBearer;
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
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;
import com.patho.main.service.PDFService;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable(preConstruction = true)
@Getter
@Setter
public class PDFCreator {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PathoConfig pathoConfig;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private PDFService pdfService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MediaRepository mediaRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ThreadPoolTaskExecutor taskExecutor;

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static String newline = System.getProperty("line.separator");

	/**
	 * Lock for creating tasks in the background
	 */
	Semaphore lock = new Semaphore(1);

	/**
	 * Directory for creating the temporary pdf
	 */
	private File workingDirectory;

	/**
	 * Directors for auxiliary files
	 */
	private File auxDirectory;

	/**
	 * Directory for saving files if an error occurred
	 */
	private File errorDirectory;

	/**
	 * Directory for creating a new pdf
	 */
	private File absoluteTargetDirectory;

	/**
	 * Directory for creating a new pdf
	 */
	private File relaviteTargetDirectory;

	/**
	 * Target file
	 */
	private File absoulteTargetFile;

	/**
	 * Relative path to the object. Used for saving in database.
	 */
	private String relativeTargetFile;

	/**
	 * Target Image file
	 */
	private File absoluteTargetImg;

	/**
	 * Relative path to the object. Used for saving in database.
	 */
	private String relativeTargetImg;

	public PDFCreator() {
		this(null);
	}

	public PDFCreator(File workdirectory) {
		if (workdirectory == null)
			workdirectory = new File(pathoConfig.getFileSettings().getPrintDirectory());

		this.relaviteTargetDirectory = workdirectory;
		this.absoluteTargetDirectory = mediaRepository.getWriteFile(workdirectory);

		this.workingDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getWorkDirectory());
		this.auxDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getAuxDirectory());
		this.errorDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getErrorDirectory());
	}

	public PDFContainer createPDF(PrintDocument template) throws FileNotFoundException {
		return createPDF(template, relaviteTargetDirectory, false);
	}

	public PDFContainer createPDF(PrintDocument template, File relaviteTargetDirectory) throws FileNotFoundException {
		return createPDF(template, relaviteTargetDirectory, false);
	}

	public PDFContainer createPDF(PrintDocument template, File relaiveTargetDirectory, boolean generateThumbnail)
			throws FileNotFoundException {
		this.relaviteTargetDirectory = relaiveTargetDirectory;
		this.absoluteTargetDirectory = mediaRepository.getWriteFile(relaiveTargetDirectory);

		PDFContainer container = getNewUniquePDF(generateThumbnail);
		container.setType(template.getDocumentType());
		container.setName(template.getGeneratedFileName());

		return runPDFCreation(template, container, generateThumbnail);
	}

	public String createPDFNonBlocking(PrintDocument template, LazyPDFReturnHandler returnHandler) {
		return createPDFNonBlocking(template, relaviteTargetDirectory, false, returnHandler);
	}

	public String createPDFNonBlocking(PrintDocument template, File relaviteTargetDirectory,
			LazyPDFReturnHandler returnHandler) {
		return createPDFNonBlocking(template, relaviteTargetDirectory, false, returnHandler);
	}

	public String createPDFNonBlocking(PrintDocument template, File relaiveTargetDirectory, boolean generateThumbnail,
			LazyPDFReturnHandler returnHandler) {

		String uuid = UUID.randomUUID().toString();

		taskExecutor.execute(new Thread() {
			public void run() {
				try {
					lock.acquire();

					logger.debug("Starting PDF Generation in new Thread");
					PDFContainer returnPDF = createPDF(template, relaiveTargetDirectory, generateThumbnail);
					returnHandler.returnPDFContent(returnPDF, uuid);
					logger.debug("PDF Generation completed, thread ended");
				} catch (Exception e) {
					logger.debug("Error");
					e.printStackTrace();
				} finally {
					lock.release();
				}
			}
		});
		return uuid;
	}

	public PDFContainer updateExistingPDF(PrintDocument template, PDFContainer container) {
		return updateExistingPDF(template, container, false);
	}

	public PDFContainer updateExistingPDF(PrintDocument template, PDFContainer container, boolean generateThumbnail) {
		this.absoluteTargetDirectory = mediaRepository
				.getParentDirectory(mediaRepository.getWriteFile(container.getPath()));
		this.relaviteTargetDirectory = mediaRepository.getParentDirectory(container.getPath());

		return runPDFCreation(template, container, generateThumbnail);
	}

	public String updateExistingPDFNonBlocking(PrintDocument template, PDFContainer container,
			LazyPDFReturnHandler returnHandler) {
		return updateExistingPDFNonBlocking(template, container, false, returnHandler);
	}

	public String updateExistingPDFNonBlocking(PrintDocument template, PDFContainer container,
			boolean generateThumbnail, LazyPDFReturnHandler returnHandler) {

		String uuid = UUID.randomUUID().toString();

		taskExecutor.execute(new Thread() {
			public void run() {
				try {

					lock.acquire();

					logger.debug("Stargin PDF Generation in new Thread");
					PDFContainer returnPDF = updateExistingPDF(template, container, generateThumbnail);
					returnHandler.returnPDFContent(returnPDF, uuid);
					logger.debug("PDF Generation completed, thread ended");
				} catch (Exception e) {
				} finally {
					lock.release();
				}
			}
		});
		return uuid;
	}

	private PDFContainer runPDFCreation(PrintDocument template, PDFContainer container, boolean generateThumbnail) {
		this.absoulteTargetFile = mediaRepository.getWriteFile(container.getPath());
		this.relativeTargetFile = container.getPath();
		this.absoluteTargetImg = mediaRepository.getWriteFile(container.getThumbnail());
		this.relativeTargetImg = container.getThumbnail();

		boolean created = run(template.getFinalContent(), generateThumbnail);

		if (!created) {
			// TODO throw error
		}

		if (template.isAfterPDFCreationHook())
			container = template.onAfterPDFCreation(container, this);

		return container;
	}

	private boolean run(String content, boolean createThumbnail) {

		PDFLatexHelper helper = null;

		try {
			if (!validateEnvironment())
				throw new IOException("Could not create environment");

			helper = initlizeTmpFiles(createThumbnail);

			if (!initlizeInputFile(helper, content))
				throw new IOException("Could not create Input files");

			if (!helper.validateInput())
				throw new IOException("Could not create Input files");

			if (!runPDFLatex(helper))
				throw new IOException("Could not create PDF");

			if (!helper.validateOutput())
				throw new IOException("Could not create PDF");

			if (createThumbnail) {
				if (!generateThumbnail(helper))
					throw new IOException("Could not create Thumbnail");

				if (!helper.validateThumbnail())
					throw new IOException("Could not create Thumbnail");
			}

			if (!absoluteTargetDirectory.equals(workingDirectory)) {
				if (!moveFileToTargetDestination(helper.outputFile, absoulteTargetFile))
					throw new IOException("Could not move PDF File");

				if (createThumbnail && !moveFileToTargetDestination(helper.outputThumbnail, absoluteTargetImg))
					throw new IOException("Could not move Thumbnail");
			}

			if (pathoConfig.getFileSettings().getCleanup()) {
				cleanUp(helper, false);
				logger.debug("Cleanup Completed");
			} else {
				logger.debug("Clean-up disabled");
				logger.debug("Tex file : {}", helper.inputFile);
			}

			return true;
		} catch (IOException e1) {

			// removing files if helper was created
			if (helper != null)
				cleanUp(helper, true);

			e1.printStackTrace();
			return false;
		}

	}

	private boolean validateEnvironment() throws FileNotFoundException {
		// checking directories
		if (!workingDirectory.isDirectory() && !workingDirectory.mkdirs()) {
			logger.error("Error directory not found: work directory {}", workingDirectory.getAbsolutePath());
			throw new FileNotFoundException(
					"Error directory not found: work directory " + workingDirectory.getAbsolutePath());
		}

		if (!auxDirectory.isDirectory() && !auxDirectory.mkdirs()) {
			logger.error("Error directory not found: aux directory {}", auxDirectory.getAbsolutePath());
			throw new FileNotFoundException(
					"Error directory not found: aux directory " + auxDirectory.getAbsolutePath());
		}

		if (!absoluteTargetDirectory.isDirectory() && !absoluteTargetDirectory.mkdirs()) {
			logger.error("Error directory not found: output directory {}", absoluteTargetDirectory.getAbsolutePath());
			throw new FileNotFoundException(
					"Error directory not found: output directory " + absoluteTargetDirectory.getAbsolutePath());
		}

		if (!errorDirectory.isDirectory() && !errorDirectory.mkdirs()) {
			logger.error("Error directory not found: error directory {}", errorDirectory.getAbsolutePath());
			throw new FileNotFoundException(
					"Error directory not found: error directory " + errorDirectory.getAbsolutePath());
		}

		return true;
	}

	private PDFLatexHelper initlizeTmpFiles(boolean thumbanil) throws FileNotFoundException {
		logger.debug("Setting up temporary files");
		String createName = mediaRepository.getUniqueName(workingDirectory, ".tex");
		PDFLatexHelper helper = new PDFLatexHelper();
		helper.setInputFile(new File(workingDirectory, createName));
		helper.setAuxFile(new File(auxDirectory, createName.replace(".tex", ".aux")));
		helper.setLogFile(new File(auxDirectory, createName.replace(".tex", ".log")));
		helper.setOutputFile(new File(workingDirectory, createName.replace(".tex", ".pdf")));
		helper.setOutputThumbnail(thumbanil ? new File(workingDirectory, createName.replace(".tex", ".png")) : null);
		return helper;
	}

	private boolean initlizeInputFile(PDFLatexHelper helper, String content) throws IOException {
		if (!mediaRepository.saveString(content, helper.getInputFile())) {
			throw new IOException("Could not save pdf data to " + helper.getInputFile().getAbsolutePath());
		}
		return true;
	}

	private boolean runPDFLatex(PDFLatexHelper helper) throws IOException {
		return runPDFLatex(helper, workingDirectory, auxDirectory);
	}

	private boolean runPDFLatex(PDFLatexHelper helper, File workingDirectory, File auxDirectory) throws IOException {
		return runPDFLatex(new File("pdflatex"), helper, workingDirectory, auxDirectory);
	}

	private boolean runPDFLatex(File pdflatex, PDFLatexHelper helper, File workingDirectory, File auxDirectory)
			throws IOException {

		helper.errorMessage = null;

		String para1 = pdflatex.getPath();
		String para2 = "--interaction=nonstopmode";
		String para3 = "--output-directory=" + workingDirectory.getAbsolutePath();
		String para4 = "--aux-directory=" + auxDirectory.getAbsolutePath();
		String para5 = helper.getInputFile().getAbsolutePath();

		ProcessBuilder localProcessBuilder = new ProcessBuilder(new String[] { para1, para2, para3, para4, para5 });
		localProcessBuilder.redirectErrorStream(true);
		localProcessBuilder.directory(workingDirectory);

		int loops = 2;

		for (int i = 1; i <= loops; i++) {
			Process localProcess = localProcessBuilder.start();

			InputStreamReader localInputStreamReader = new InputStreamReader(localProcess.getInputStream());
			StringBuilder localStringBuilder = new StringBuilder();

			try (BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader)) {
				String res = null;
				while ((res = localBufferedReader.readLine()) != null) {
					localStringBuilder.append(res + newline);
				}
			}

			helper.generationResult = localStringBuilder.toString();

			try {
				int j = localProcess.waitFor();

				if (j != 0) {
					helper.errorMessage = ("Errors occurred while executing pdfLaTeX! Exit value of the process: " + j);
				}

			} catch (InterruptedException localInterruptedException) {
				helper.errorMessage = ("The process pdfLaTeX was interrupted and an exception occurred!" + newline
						+ localInterruptedException.toString());
			}

		}
		return true;
	}

	private boolean generateThumbnail(PDFLatexHelper helper) {
		if (helper.getOutputThumbnail() == null)
			return false;

		pdfService.createThumbnail(helper.getOutputThumbnail(), helper.getOutputFile(), 0,
				pathoConfig.getFileSettings().getThumbnailDPI());

		logger.debug("Thumbail created: " + helper.getOutputThumbnail().getPath());

		return true;
	}

	private PDFContainer getNewUniquePDF(boolean thumbnail) throws FileNotFoundException {
		String outPutName = mediaRepository.getUniqueName(absoluteTargetDirectory, ".pdf");
		File outFile = new File(relaviteTargetDirectory, outPutName);
		File outImg = new File(relaviteTargetDirectory, outPutName.replace(".pdf", ".png"));

		logger.debug("PDF with output file " + outFile.getPath() + " dir " + absoluteTargetDirectory.getPath());
		PDFContainer pdfContainer = new PDFContainer();
		pdfContainer.setPath(outFile.getPath());
		pdfContainer.setThumbnail(thumbnail ? outImg.getPath() : "");
		return pdfContainer;
	}

	private void cleanUp(PDFLatexHelper helper, boolean error) {
		if (!error) {
			mediaRepository.delete(helper.inputFile);
			mediaRepository.delete(helper.auxFile);
			mediaRepository.delete(helper.logFile);
			if (!absoluteTargetDirectory.equals(workingDirectory)) {
				mediaRepository.delete(helper.outputFile);
				if (helper.outputThumbnail != null)
					mediaRepository.delete(helper.outputThumbnail);
			}

		} else {
			if (pathoConfig.getFileSettings().getKeepErrorFiles()) {
				logger.debug("Moving files to error directory");
				mediaRepository.moveFile(helper.inputFile, errorDirectory);
				mediaRepository.moveFile(helper.auxFile, errorDirectory);
				mediaRepository.moveFile(helper.logFile, errorDirectory);
				mediaRepository.moveFile(helper.outputFile, errorDirectory);
				if (helper.outputThumbnail != null)
					mediaRepository.moveFile(helper.outputThumbnail, errorDirectory);
				if (!absoluteTargetDirectory.equals(workingDirectory)) {
					mediaRepository.moveFile(absoulteTargetFile, errorDirectory);
					if (absoluteTargetImg != null)
						mediaRepository.moveFile(absoluteTargetImg, errorDirectory);
				}
			} else {
				mediaRepository.delete(helper.inputFile);
				mediaRepository.delete(helper.auxFile);
				mediaRepository.delete(helper.logFile);
				mediaRepository.delete(helper.outputFile);
				if (helper.outputThumbnail != null)
					mediaRepository.delete(helper.outputThumbnail);
				if (!absoluteTargetDirectory.equals(workingDirectory)) {
					mediaRepository.delete(absoulteTargetFile);
					if (absoluteTargetImg != null)
						mediaRepository.delete(absoluteTargetImg);
				}
			}
		}
	}

	private boolean moveFileToTargetDestination(File srcFile, File destFile) {
		return moveFileToTargetDestination(srcFile, destFile, true);
	}

	private boolean moveFileToTargetDestination(File srcFile, File destFile, boolean overwrite) {
		return mediaRepository.moveFile(srcFile, destFile);
	}

	public PDFContainer mergePDFs(PDFContainer target, List<LoadedPrintPDFBearer> containers) {
		LoadedPrintPDFBearer loadedContainer = PDFCreator.mergePdfs(containers, "", target.getType());

		mediaRepository.saveBytes(loadedContainer.getPdfData(), target.getPath());

		if (target.getThumbnail() != null)
			pdfService.createThumbnail(new File(target.getThumbnail()), loadedContainer.getPdfData());

		return target;
	}

	/**
	 * Returns the amount of pdf Pages
	 * 
	 * @param container
	 * @return
	 */
	public static int countPDFPages(LoadedPrintPDFBearer container) {
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
	public static LoadedPrintPDFBearer mergePdfs(List<LoadedPrintPDFBearer> containers, String name, DocumentType type) {

		try {
			Document document = new Document();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			for (LoadedPrintPDFBearer pdfContainer : containers) {
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

			return new LoadedPrintPDFBearer(type, name, out.toByteArray(), null);
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Setter
	@Getter
	private class PDFLatexHelper {

		private File inputFile;
		private File auxFile;
		private File logFile;
		private File outputFile;
		private File outputThumbnail;

		/**
		 * If an error occurs it is stored here
		 */
		private String errorMessage;

		/**
		 * Return of pdf latex after creation
		 */
		private String generationResult;

		public boolean validateInput() {
			if (!inputFile.isFile()) {
				logger.error("Input file not Found! " + inputFile.getAbsolutePath());
				return false;
			}

			return true;
		}

		public boolean validateOutput() {
			boolean result = true;

			if (!auxFile.isFile()) {
				result = false;
				logger.error("aux file not Found! " + auxFile.getAbsolutePath());
			}

			if (!logFile.isFile()) {
				result = false;
				logger.error("log file not Found! " + logFile.getAbsolutePath());
			}

			if (!outputFile.isFile()) {
				result = false;
				logger.error("out file not Found! " + outputFile.getAbsolutePath());
			}

			return result;
		}

		public boolean validateThumbnail() {
			if (outputThumbnail != null && outputThumbnail.isFile()) {
				return true;
			}
			return false;
		}

		public void printToLog() {
			logger.debug("Working directory: " + workingDirectory.getAbsolutePath());
			logger.debug("Aux directory: " + auxDirectory.getAbsolutePath());
			logger.debug("Output directory: " + absoluteTargetDirectory.getAbsolutePath());
			logger.debug("Input file: " + inputFile.getAbsolutePath());
			logger.debug("Output file: " + outputFile.getPath());
		}
	}
}
