package com.patho.main.util.pdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.config.PathoConfig;
import com.patho.main.config.PathoConfig.DefaultDocuments;
import com.patho.main.config.PathoConfig.DefaultNotification;
import com.patho.main.config.PathoConfig.FileSettings;
import com.patho.main.config.PathoConfig.Miscellaneous;
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;
import com.patho.main.service.PDFService;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.pdf.PDFGenerator.PDFDataAndPathContainer;
import com.patho.main.util.version.VersionContainer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
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

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static String newline = System.getProperty("line.separator");

	/**
	 * Lock for creating tasks in the background
	 */
	final Object lock = new Object();

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
		this.workingDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getWorkDirectory());
		this.auxDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getAuxDirectory());
		this.errorDirectory = mediaRepository.getWriteFile(pathoConfig.getFileSettings().getErrorDirectory());
	}

	public PDFContainer createPDF(PrintDocument template, File relaiveTargetDirectory) {
		return createPDF(template, relaiveTargetDirectory, false)
	}

	public PDFContainer createPDF(PrintDocument template, File relaiveTargetDirectory, boolean generateThumbnail) {
		this.relaviteTargetDirectory = relaiveTargetDirectory;
		this.absoluteTargetDirectory = mediaRepository.getWriteFile(relaiveTargetDirectory);

		PDFContainer container = getNewUniquePDF(generateThumbnail);
		container.setType(template.getDocumentType());
		container.setName(template.getGeneratedFileName());

		this.absoulteTargetFile = mediaRepository.getWriteFile(container.getPath());
		this.relativeTargetFile = container.getPath();
		this.absoluteTargetImg = mediaRepository.getWriteFile(container.getThumbnail());
		this.relativeTargetImg = container.getThumbnail();
		
		return createPDF(template, outputDirectory, null, generateThumbnail);
	}

	public PDFContainer updateExistingPDF(PrintDocument template, PDFContainer container, boolean generateThumbnail) {
		this.absoluteTargetDirectory = mediaRepository
				.getParentDirectory(mediaRepository.getWriteFile(container.getPath()));
		this.relaviteTargetDirectory = mediaRepository.getParentDirectory(container.getPath());
		this.absoulteTargetFile = mediaRepository.getWriteFile(container.getPath());
		this.relativeTargetFile = container.getPath();
		this.absoluteTargetImg = mediaRepository.getWriteFile(container.getThumbnail());
		this.relativeTargetImg = container.getThumbnail();

		return createNewPDF(template, new File(pathoConfig.getFileSettings().getPrintDirectory()), container,
				generateThumbnail);
	}

	public PDFContainer updateExistingPDF(PrintDocument template, PDFContainer container) {
		return updateExistingPDF(template, container, false);
	}

	private PDFContainer runPDFCreation(PrintDocument template, boolean createThumbnail) {

		if (!validateEnvironment())
			return null;

		PDFLatexHelper helper = initlizeTmpFiles(createThumbnail);

		if (!initlizeInputFile(helper, template.getFinalContent()))
			return null;

		if (!helper.validateInput())
			return null;

		try {
			if (!runPDFLatex(helper))
				return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		if (!helper.validateOutput())
			return null;

		if (createThumbnail) {
			if (!generateThumbnail(helper))
				return null;

			if (!helper.validateInput())
				return null;
		}
		
		if(!absoluteTargetDirectory.equals(workingDirectory)) {
			
		}

	}

	private boolean validateEnvironment() {
		// checking directories
		if (!workingDirectory.isDirectory() && !workingDirectory.mkdirs()) {
			logger.error("Error directory not found: work directory {}", workingDirectory.getAbsolutePath());
			throw new IllegalArgumentException(
					"Error directory not found: work directory " + workingDirectory.getAbsolutePath());
		}

		if (!auxDirectory.isDirectory() && !auxDirectory.mkdirs()) {
			logger.error("Error directory not found: aux directory {}", auxDirectory.getAbsolutePath());
			throw new IllegalArgumentException(
					"Error directory not found: aux directory " + auxDirectory.getAbsolutePath());
		}

		if (!absoluteTargetDirectory.isDirectory() && !absoluteTargetDirectory.mkdirs()) {
			logger.error("Error directory not found: output directory {}", absoluteTargetDirectory.getAbsolutePath());
			throw new IllegalArgumentException(
					"Error directory not found: output directory " + absoluteTargetDirectory.getAbsolutePath());
		}

		if (!errorDirectory.isDirectory() && !errorDirectory.mkdirs()) {
			logger.error("Error directory not found: error directory {}", errorDirectory.getAbsolutePath());
			throw new IllegalArgumentException(
					"Error directory not found: error directory " + errorDirectory.getAbsolutePath());
		}

		return true;
	}

	private PDFLatexHelper initlizeTmpFiles(boolean thumbanil) {
		String createName = mediaRepository.getUniqueName(workingDirectory, ".tex");
		PDFLatexHelper helper = new PDFLatexHelper();
		helper.setInputFile(new File(workingDirectory, createName));
		helper.setAuxFile(new File(auxDirectory, createName.replace(".tex", ".aux")));
		helper.setLogFile(new File(auxDirectory, createName.replace(".tex", ".log")));
		helper.setOutputFile(new File(workingDirectory, createName.replace(".tex", ".pdf")));
		helper.setOutputThumbnail(thumbanil ? new File(workingDirectory, createName.replace(".tex", ".png")) : null);
		return helper;
	}

	private boolean initlizeInputFile(PDFLatexHelper helper, String content) {
		if (!mediaRepository.saveString(content, helper.getInputFile())) {
			throw new IllegalArgumentException(
					"Could not save pdf to process to " + helper.getInputFile().getAbsolutePath());
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

	private PDFContainer getNewUniquePDF(boolean thumbnail) {
		String outPutName = mediaRepository.getUniqueName(absoluteTargetDirectory, ".pdf");
		File outFile = new File(relaviteTargetDirectory, outPutName);
		File outImg = new File(relaviteTargetDirectory, outPutName.replace(".pdf", ".png"));

		logger.debug("PDF with output file " + outFile.getPath() + " dir " + absoluteTargetDirectory.getPath());
		PDFContainer pdfContainer = new PDFContainer();
		pdfContainer.setPath(outFile.getPath());
		pdfContainer.setThumbnail(thumbnail ? outImg.getPath() : null);
		return pdfContainer;
	}
	
	private boolean moveFileToTargetDestination(File file, File target) {
		FileUtils.moveDirectory(srcDir, destDir);
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
