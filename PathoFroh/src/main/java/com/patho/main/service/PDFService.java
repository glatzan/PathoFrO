package com.patho.main.service;

import com.patho.main.config.PathoConfig;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.miscellaneous.BioBank;
import com.patho.main.model.patient.miscellaneous.Council;
import com.patho.main.repository.jpa.*;
import com.patho.main.repository.miscellaneous.MediaRepository;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.pdf.LazyPDFReturnHandler;
import com.patho.main.util.pdf.PDFCreator;
import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Creates a new empty pdf with no file on the hard drive. This is a "virtual"
     * pdf.
     */
    public PDFReturn createAndAttachPDF(DataList dataList, PDFInfo pdfInfo) throws FileNotFoundException {
        return createAndAttachPDF(dataList, pdfInfo, null, false);
    }

    /**
     * Creates a new pdf and saves the content to the hard drive. If a target folder
     * is passed via pdfInfo, the targetFolder will be used, otherwise the dataLists
     * targetFolder is used.
     */
    public PDFReturn createAndAttachPDF(DataList dataList, PDFInfo pdfInfo, byte[] content)
            throws FileNotFoundException {
        return createAndAttachPDF(dataList, pdfInfo, content, false);
    }

    /**
     * Creates a new pdf and saves the content to the hard drive. If a target folder
     * is passed via pdfInfo, the targetFolder will be used, otherwise the dataLists
     * targetFolder is used.
     */
    public PDFReturn createAndAttachPDF(DataList dataList, PDFInfo pdfInfo, byte[] content, boolean createThumbnail)
            throws FileNotFoundException {

        PDFContainer pdf = newPDF(pdfInfo,
                pdfInfo.getTargetFolder() != null ? pdfInfo.getTargetFolder() : dataList.getFileRepositoryBase());

        pdf = pdfRepository.save(pdf);
        dataList = saveORAttachPDF(dataList, pdf);

        try {
            if (content != null) {
                logger.debug("Saving pdf to disk");
                mediaRepository.saveBytes(content, pdf.getPath());
                if (createThumbnail) {
                    logger.debug("Creating thumbnail");
                    mediaRepository.saveImage(
                            gerateThumbnail(content, 0, pathoConfig.getFileSettings().getThumbnailDPI()),
                            pdf.getThumbnail());
                }
            } else
                logger.debug("Virtual PDF no content!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Error while saving file, removing file from datalists");
            dataList = deletePdf(dataList, pdf);
            throw new IllegalAccessError("Could not updaload data");
        }

        return new PDFReturn(dataList, pdf);
    }

    /**
     * Creates a new PDF Object an runs the PDFCreator in order to create a pdf from
     * the printDocument
     */
    public PDFReturn createAndAttachPDF(DataList dataList, PrintDocument printDocument) throws FileNotFoundException {
        return createAndAttachPDF(dataList, printDocument, new PDFInfo(printDocument.getGeneratedFileName(),
                printDocument.getDocumentType(), "", "", dataList.getFileRepositoryBase()), false, false, null);
    }

    /**
     * Creates a new PDF Object an runs the PDFCreator in order to create a pdf from
     * the printDocument
     */
    public PDFReturn createAndAttachPDF(DataList dataList, PrintDocument printDocument, boolean createThumbnail)
            throws FileNotFoundException {
        return createAndAttachPDF(
                dataList, printDocument, new PDFInfo(printDocument.getGeneratedFileName(),
                        printDocument.getDocumentType(), "", "", dataList.getFileRepositoryBase()),
                createThumbnail, false, null);
    }

    /**
     * Creates a new PDF Object an runs the PDFCreator in order to create a pdf from
     * the printDocument. For PDF Object creation data from the pdfInfo objects are
     * used.
     */
    public PDFReturn createAndAttachPDF(DataList dataList, PrintDocument printDocument, PDFInfo pdfInfo)
            throws FileNotFoundException {
        return createAndAttachPDF(dataList, printDocument, pdfInfo, false, false, null);
    }

    /**
     * Creates a new PDF Object an runs the PDFCreator in order to create a pdf from
     * the printDocument. For PDF Object creation data from the pdfInfo objects are
     * used. If nonBlocking is true the new PDf Obejct will be returnd and the pdf
     * will be generated in a new Thread.
     */
    public PDFReturn createAndAttachPDF(DataList dataList, PrintDocument printDocument, PDFInfo pdfInfo,
                                        boolean createThumbnail, boolean nonBlocking, LazyPDFReturnHandler returnHandler)
            throws FileNotFoundException {
        PDFContainer pdf = newPDF(pdfInfo,
                pdfInfo.getTargetFolder() != null ? pdfInfo.getTargetFolder() : dataList.getFileRepositoryBase());

        pdf = pdfRepository.save(pdf);
        dataList = saveORAttachPDF(dataList, pdf);

        try {
            if (!nonBlocking)
                pdf = new PDFCreator().updateExistingPDF(printDocument, pdf, createThumbnail);
            else {
                new PDFCreator().updateExistingPDFNonBlocking(printDocument, pdf, createThumbnail,
                        returnHandler != null ? returnHandler : new LazyPDFReturnHandler() {
                            @Override
                            public void returnPDFContent(PDFContainer container, String uuid) {
                                logger.debug("Returning PDF Creation withou result");
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("Error while saving file, removing file from datalists");
            // do not delete
            // TODO handle erro
            // dataList = deletePdf(dataList, pdf);
            throw new IllegalAccessError("Could not updaload data");
        }

        return new PDFReturn(dataList, pdf);
    }

    /**
     * Updates a already created PDF Object. If nonBlocking is true, the pdf
     * creation will be done in a seperate thread.
     */
    public PDFContainer updateAttachedPDF(PDFContainer pdfContainer, PrintDocument printDocument,
                                          boolean createThumbnail, boolean nonBlocking) {
        if (!nonBlocking)
            pdfContainer = new PDFCreator().updateExistingPDF(printDocument, pdfContainer, createThumbnail);
        else {
            new PDFCreator().updateExistingPDFNonBlocking(printDocument, pdfContainer, createThumbnail,
                    new LazyPDFReturnHandler() {
                        @Override
                        public void returnPDFContent(PDFContainer container, String uuid) {
                        }
                    });
        }
        return pdfContainer;
    }

    /**
     * Removes a pdf form the datalist and saves the change.
     */
    public DataList removePdf(DataList dataList, PDFContainer pdfContainer) {
        dataList.removeReport(pdfContainer);
        return saveDataList(dataList, resourceBundle.get("log.pdf.removed", pdfContainer.getName()));
    }

    /**
     * Removes a pdf form the datalist and deletes the content from the hard drive.
     * Als this method call will save all changes to the database.
     */
    public DataList deletePdf(DataList dataList, PDFContainer pdfContainer) {

        dataList = removePdf(dataList, pdfContainer);

        if (pdfContainer.getPath() != null)
            mediaRepository.delete(pdfContainer.getPath());
        if (pdfContainer.getThumbnail() != null)
            mediaRepository.delete(pdfContainer.getThumbnail());

        pdfRepository.delete(pdfContainer);

        return dataList;
    }

    /**
     * Moves a pdf from one datalist to the target datalist. Notice the pdf storen
     * on the hard disk will not be moved if the targetDatalist has an other folder.
     */
    public void movePdf(DataList from, DataList to, PDFContainer pdfContainer) {
        removePdf(from, pdfContainer);
        saveORAttachPDF(to, pdfContainer);
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

        saveORAttachPDF(to, pdfContainer);
    }

    public void copyPdf() {

    }

    /**
     * Adds a pdf to the datalist and saves the datalist depending on the datalists
     * type.
     */
    private DataList saveORAttachPDF(DataList dataList, PDFContainer pdfContainer) {
        dataList.getAttachedPdfs().add(pdfContainer);
        return saveDataList(dataList, resourceBundle.get("log.pdf.uploaded", pdfContainer.getName()));
    }

    /**
     * Saves the datalist depending on the dtatalists type.
     */
    public DataList saveDataList(DataList dataList, String log) {
        if (dataList instanceof Patient) {
            return patientRepository.save((Patient) dataList, log);
        } else if (dataList instanceof Task) {
            return taskRepository.save((Task) dataList, log);
        } else if (dataList instanceof BioBank) {
            return bioBankRepository.save((BioBank) dataList, log);
        } else if (dataList instanceof Council) {
            return councilRepository.save((Council) dataList, log);
        } else {
            throw new IllegalArgumentException("List type not supported (" + dataList + ")");
        }
    }

    /**
     * Creates a new PDF Object, does not save PDF Data to database.
     */
    private PDFContainer newPDF(PDFInfo pdfInfo, File targetFolder) throws FileNotFoundException {
        return newPDF(pdfInfo.getName(), pdfInfo.getDocumentType(), pdfInfo.getCommentary(),
                pdfInfo.getCommentaryIntern(), targetFolder);
    }

    /**
     * Creates a new PDF Object, does not save PDF Data to database.
     */
    private PDFContainer newPDF(String name, PrintDocumentType documentType, String commentary, String commentaryIntern,
                                File targetFolder) throws FileNotFoundException {
        PDFContainer pdf = new PDFContainer();
        pdf.setName(name);
        pdf.setType(documentType);
        pdf.setCommentary(commentary);
        pdf.setIntern(commentaryIntern);
        getUniquePDF(targetFolder, pdf);
        return pdf;
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

    public File createThumbnail(File output, byte[] pdf) {
        return createThumbnail(output, pdf, 0, pathoConfig.getFileSettings().getThumbnailDPI());
    }

    public File createThumbnail(File output, File input, int pageNo, int dpi) {
        byte[] load = mediaRepository.getBytes(input.getPath());
        return createThumbnail(output, load, pageNo, dpi);
    }

    public File createThumbnail(File output, byte[] pdf, int pageNo, int dpi) {
        mediaRepository.saveImage(gerateThumbnail(pdf, 0, pathoConfig.getFileSettings().getThumbnailDPI()), output);
        return output;
    }

    public BufferedImage gerateThumbnail(File path, int pageNo, int dpi) {
        return gerateThumbnail(mediaRepository.getBytes(path.getPath()), pageNo, dpi);
    }

    /**
     * Returns a buffered Image from a pdf page, page number is zero based
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

    /**
     * Gets a uinique file within the folder
     */
    public PDFContainer getUniquePDF(File folder) throws FileNotFoundException {
        return getUniquePDF(folder, new PDFContainer());
    }

    /**
     * Gets a uinique file within the folder
     */
    public PDFContainer getUniquePDF(File folder, PDFContainer container) throws FileNotFoundException {
        File uniqueFile = new File(folder, mediaRepository.getUniqueName(folder, ".pdf"));
        container.setPath(uniqueFile.getPath());
        container.setThumbnail(uniqueFile.getPath().replace(".pdf", ".png"));
        return container;
    }

    /**
     * Search for the parentdatalist of the given pdf.
     */
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
     */
    public static DataList getDatalistFromDatalists(List<DataList> dataLists, DataList findeThisList) {
        for (DataList dataList : dataLists) {
            if (dataList.getId() == findeThisList.getId() && dataList.getClass().equals(findeThisList.getClass()))
                return dataList;
        }

        return null;
    }

    /**
     * Returns a list of pdfs with the given type.
     */
    public static List<PDFContainer> findPDFsByDocumentType(Set<PDFContainer> containers, PrintDocumentType type) {
        return containers.stream().filter(p -> p.getType().equals(type)).collect(Collectors.toList());
    }

    /**
     * Returns the pdf with the most recent creation date.
     */
    public static PDFContainer findNewestPDF(Set<PDFContainer> containers) {
        if (containers.size() == 0)
            return null;

        PDFContainer latest = HistoUtil.getFirstElement(containers);

        for (PDFContainer pdfContainer : containers) {
            if (latest.getAudit().getCreatedOn() < pdfContainer.getAudit().getCreatedOn())
                latest = pdfContainer;
        }

        return latest;
    }

    /**
     * Sorts an set an retuns it as soreted list. (Sorted by creation date)
     */
    public static List<PDFContainer> sortPDFsByDate(Set<PDFContainer> set, boolean asc) {
        List<PDFContainer> list = new ArrayList<PDFContainer>(set);

        // sorting
        Collections.sort(list, (PDFContainer p1, PDFContainer p2) -> {
            if (p1.getAudit().getCreatedOn() == p2.getAudit().getCreatedOn()) {
                return 0;
            } else if (p1.getAudit().getCreatedOn() < p2.getAudit().getCreatedOn()) {
                return asc ? -1 : 1;
            } else {
                return asc ? 1 : -1;
            }
        });

        return list;
    }

    /**
     * Searches for a matching reportIntent report by internal id.
     *
     * @param task
     * @param diagnosisRevision
     * @return
     */
    public static Optional<PDFContainer> findDiagnosisReport(Task task, DiagnosisRevision diagnosisRevision) {
        String matcher = PDFContainer.MARKER_DIAGNOSIS.replace("$id", String.valueOf(diagnosisRevision.getId()));
        for (PDFContainer container : task.getAttachedPdfs()) {
            if (container.getIntern() != null && container.getIntern().matches(matcher)) {
                return Optional.ofNullable(container);
            }
        }
        return Optional.empty();
    }

    /**
     * Return class for returning the new pdf an the datalist
     *
     * @author dvk-glatza
     */
    public static class PDFReturn {
        DataList dataList;
        PDFContainer container;

        public PDFReturn(DataList dataList, PDFContainer container) {
            this.dataList = dataList;
            this.container = container;
        }

        public DataList getDataList() {
            return this.dataList;
        }

        public PDFContainer getContainer() {
            return this.container;
        }

        public void setDataList(DataList dataList) {
            this.dataList = dataList;
        }

        public void setContainer(PDFContainer container) {
            this.container = container;
        }
    }

    /**
     * Helper class for creating or updating pdfs.
     *
     * @author dvk-glatza
     */
    @Getter
    @Setter
    public static class PDFInfo {
        private String name;
        private PrintDocumentType documentType;
        private String commentary;
        private String commentaryIntern;
        private File targetFolder;

        public PDFInfo(String name, PrintDocumentType documentType) {
            this(name, documentType, "", "", null);
        }

        public PDFInfo(String name, PrintDocumentType documentType, File targetFolder) {
            this(name, documentType, "", "", targetFolder);
        }

        public PDFInfo(String name, PrintDocumentType documentType, String commentary, String commentaryIntern) {
            this(name, documentType, commentary, commentaryIntern, null);
        }

        public PDFInfo(String name, PrintDocumentType documentType, String commentary, String commentaryIntern,
                       File targetFolder) {
            this.name = name;
            this.documentType = documentType;
            this.commentary = commentary;
            this.commentaryIntern = commentaryIntern;
            this.targetFolder = targetFolder;
        }
    }
}
