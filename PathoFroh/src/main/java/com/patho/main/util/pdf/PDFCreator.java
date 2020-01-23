package com.patho.main.util.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.patho.main.model.PDFContainer;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.print.LoadedPrintPDFBearer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Getter
@Setter
public class PDFCreator {

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
    private File targetDirectory;

    /**
     * Relative path to the object. Used for saving in database.
     */
    private String targetFile;

    /**
     * Relative path to the object. Used for saving in database.
     */
    private String targetImageFile;

    public PDFCreator() {
        this(null);
    }

    public PDFCreator(File workdirectory) {
        if (workdirectory == null)
            workdirectory = new File(SpringContextBridge.services().getPathoConfig().getFileSettings().getPrintDirectory());

        this.targetDirectory = workdirectory;

        this.workingDirectory = new File(SpringContextBridge.services().getPathoConfig().getFileSettings().getWorkDirectory());
        this.auxDirectory = new File(SpringContextBridge.services().getPathoConfig().getFileSettings().getAuxDirectory());
        this.errorDirectory = new File(SpringContextBridge.services().getPathoConfig().getFileSettings().getErrorDirectory());
    }

    public PDFContainer createPDF(PrintDocument template) throws FileNotFoundException {
        return createPDF(template, targetDirectory, false);
    }

    public PDFContainer createPDF(PrintDocument template, File relaviteTargetDirectory) throws FileNotFoundException {
        return createPDF(template, relaviteTargetDirectory, false);
    }

    public PDFContainer createPDF(PrintDocument template, File targetDirectory, boolean generateThumbnail)
            throws FileNotFoundException {
        this.targetDirectory = targetDirectory;

        PDFContainer container = getNewUniquePDF(generateThumbnail);
        container.setType(template.getDocumentType());
        container.setName(template.getGeneratedFileName());

        return runPDFCreation(template, container, generateThumbnail);
    }

    public String createPDFNonBlocking(PrintDocument template, LazyPDFReturnHandler returnHandler) {
        return createPDFNonBlocking(template, targetDirectory, false, returnHandler);
    }

    public String createPDFNonBlocking(PrintDocument template, File relaviteTargetDirectory,
                                       LazyPDFReturnHandler returnHandler) {
        return createPDFNonBlocking(template, relaviteTargetDirectory, false, returnHandler);
    }

    public String createPDFNonBlocking(PrintDocument template, File relaiveTargetDirectory, boolean generateThumbnail,
                                       LazyPDFReturnHandler returnHandler) {

        String uuid = UUID.randomUUID().toString();

        SpringContextBridge.services().getTaskExecutor().execute(new Thread() {
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
        this.targetDirectory = new File((!container.getPath().startsWith("file:")) ? "file:" : "" + container.getPath());
        return runPDFCreation(template, container, generateThumbnail);
    }

    public String updateExistingPDFNonBlocking(PrintDocument template, PDFContainer container,
                                               LazyPDFReturnHandler returnHandler) {
        return updateExistingPDFNonBlocking(template, container, false, returnHandler);
    }

    public String updateExistingPDFNonBlocking(PrintDocument template, PDFContainer container,
                                               boolean generateThumbnail, LazyPDFReturnHandler returnHandler) {

        String uuid = UUID.randomUUID().toString();

        SpringContextBridge.services().getTaskExecutor().execute(new Thread() {
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
        this.targetFile = container.getPath();
        this.targetImageFile = container.getThumbnail();

        boolean created = run(template.getFinalContent(), generateThumbnail);

        if (!created) {
            // TODO throw error
        }

        if (template.getAfterPDFCreationHook())
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

            if (!targetDirectory.equals(workingDirectory)) {
                if (!moveFileToTargetDestination(helper.outputFile, targetDirectory))
                    throw new IOException("Could not move PDF File");

                if (createThumbnail && !moveFileToTargetDestination(helper.outputThumbnail, new File(targetImageFile)))
                    throw new IOException("Could not move Thumbnail");
            }

            if (SpringContextBridge.services().getPathoConfig().getFileSettings().getCleanup()) {
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

        if (!targetDirectory.isDirectory() && !absoluteTargetDirectory.mkdirs()) {
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
        String createName = SpringContextBridge.services().getMediaRepository().getUniqueName(workingDirectory, ".tex");
        PDFLatexHelper helper = new PDFLatexHelper();
        helper.setInputFile(new File(workingDirectory, createName));
        helper.setAuxFile(new File(auxDirectory, createName.replace(".tex", ".aux")));
        helper.setLogFile(new File(auxDirectory, createName.replace(".tex", ".log")));
        helper.setOutputFile(new File(workingDirectory, createName.replace(".tex", ".pdf")));
        helper.setOutputThumbnail(thumbanil ? new File(workingDirectory, createName.replace(".tex", ".png")) : null);
        return helper;
    }

    private boolean initlizeInputFile(PDFLatexHelper helper, String content) throws IOException {
        if (!SpringContextBridge.services().getMediaRepository().saveString(content, helper.getInputFile())) {
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

        ProcessBuilder localProcessBuilder = new ProcessBuilder(new String[]{para1, para2, para3, para4, para5});
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

        SpringContextBridge.services().getPdfService().createThumbnail(helper.getOutputThumbnail(), helper.getOutputFile(), 0,
                SpringContextBridge.services().getPathoConfig().getFileSettings().getThumbnailDPI());

        logger.debug("Thumbail created: " + helper.getOutputThumbnail().getPath());

        return true;
    }

    private PDFContainer getNewUniquePDF(boolean thumbnail) throws FileNotFoundException {
        String outPutName = "file:" + SpringContextBridge.services().getMediaRepository().getUniqueName(absoluteTargetDirectory, ".pdf");
        File outFile = new File(targetDirectory, outPutName);
        File outImg = new File(targetDirectory, outPutName.replace(".pdf", ".png"));

        logger.debug("PDF with output file " + outFile.getPath() + " dir " + absoluteTargetDirectory.getPath());
        PDFContainer pdfContainer = new PDFContainer();
        pdfContainer.setPath(outFile.getPath());
        pdfContainer.setThumbnail(thumbnail ? outImg.getPath() : "");
        return pdfContainer;
    }

    private void cleanUp(PDFLatexHelper helper, boolean error) {
        if (!error) {
            SpringContextBridge.services().getMediaRepository().delete(helper.inputFile);
            SpringContextBridge.services().getMediaRepository().delete(helper.auxFile);
            SpringContextBridge.services().getMediaRepository().delete(helper.logFile);
            if (!absoluteTargetDirectory.equals(workingDirectory)) {
                SpringContextBridge.services().getMediaRepository().delete(helper.outputFile);
                if (helper.outputThumbnail != null)
                    SpringContextBridge.services().getMediaRepository().delete(helper.outputThumbnail);
            }

        } else {
            if (SpringContextBridge.services().getPathoConfig().getFileSettings().getKeepErrorFiles()) {
                logger.debug("Moving files to error directory");
                SpringContextBridge.services().getMediaRepository().moveFile(helper.inputFile, errorDirectory);
                SpringContextBridge.services().getMediaRepository().moveFile(helper.auxFile, errorDirectory);
                SpringContextBridge.services().getMediaRepository().moveFile(helper.logFile, errorDirectory);
                SpringContextBridge.services().getMediaRepository().moveFile(helper.outputFile, errorDirectory);
                if (helper.outputThumbnail != null)
                    SpringContextBridge.services().getMediaRepository().moveFile(helper.outputThumbnail, errorDirectory);
                if (!absoluteTargetDirectory.equals(workingDirectory)) {
                    SpringContextBridge.services().getMediaRepository().moveFile(absoulteTargetFile, errorDirectory);
                    if (absoluteTargetImg != null)
                        SpringContextBridge.services().getMediaRepository().moveFile(absoluteTargetImg, errorDirectory);
                }
            } else {
                SpringContextBridge.services().getMediaRepository().delete(helper.inputFile);
                SpringContextBridge.services().getMediaRepository().delete(helper.auxFile);
                SpringContextBridge.services().getMediaRepository().delete(helper.logFile);
                SpringContextBridge.services().getMediaRepository().delete(helper.outputFile);
                if (helper.outputThumbnail != null)
                    SpringContextBridge.services().getMediaRepository().delete(helper.outputThumbnail);
                if (!absoluteTargetDirectory.equals(workingDirectory)) {
                    SpringContextBridge.services().getMediaRepository().delete(absoulteTargetFile);
                    if (absoluteTargetImg != null)
                        SpringContextBridge.services().getMediaRepository().delete(absoluteTargetImg);
                }
            }
        }
    }

    private boolean moveFileToTargetDestination(File srcFile, File destFile) {
        return moveFileToTargetDestination(srcFile, destFile, true);
    }

    private boolean moveFileToTargetDestination(File srcFile, File destFile, boolean overwrite) {
        return SpringContextBridge.services().getMediaRepository().moveFile(srcFile, destFile);
    }

    public PDFContainer mergePDFs(PDFContainer target, List<LoadedPrintPDFBearer> containers) {
        LoadedPrintPDFBearer loadedContainer = PDFCreator.mergePdfs(containers, "", target.getType());

        SpringContextBridge.services().getMediaRepository().saveBytes(loadedContainer.getPdfData(), target.getPath());

        if (target.getThumbnail() != null)
            SpringContextBridge.services().getPdfService().createThumbnail(new File(target.getThumbnail()), loadedContainer.getPdfData());

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
    public static LoadedPrintPDFBearer mergePdfs(List<LoadedPrintPDFBearer> containers, String name, PrintDocumentType type) {

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
