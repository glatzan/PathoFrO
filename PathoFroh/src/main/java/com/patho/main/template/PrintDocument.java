package com.patho.main.template;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.PDFContainer;
import com.patho.main.util.DateTool;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.helper.TextToLatexConverter;
import com.patho.main.util.helper.TimeUtil;
import com.patho.main.util.pdf.PDFCreator;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configurable
public class PrintDocument extends AbstractTemplate {

    @Autowired
    protected ResourceBundle resourceBundle;

    /**
     * Class of the ui Element
     */
    protected String uiClass;

    /**
     * Group of uis which share the same data context. If 0 there is no shared
     * group.
     */
    protected int sharedContextGroup;

    /**
     * Raw file content from hdd
     */
    protected String loadedContent;

    /**
     * Processed final document
     */
    public String finalContent;

    /**
     * If true the pdf generator will call onAfterPDFCreation to allow the template
     * to change or attach other things to the pdf
     */
    protected boolean afterPDFCreationHook;

    /**
     * If true duplex printing is used per default
     */
    protected boolean duplexPrinting;

    /**
     * Only in use if duplexPrinting is true. IF printEvenPageCounts is true a blank
     * page will be added if there is an odd number of pages to print.
     */
    protected boolean printEvenPageCount;

    public PrintDocument() {
    }

    public PrintDocument(DocumentType documentType) {
        setType(documentType.name());
    }

    public PrintDocument(PrintDocument document) {
        copyIntoDocument(document);
    }

    /**
     * Is called if afterPDFCreationHook is set and the pdf was created.
     *
     * @param container
     * @return
     */
    public PDFContainer onAfterPDFCreation(PDFContainer container, PDFCreator creator) {
        return container;
    }

    public PrintDocument initilize(InitializeToken... token) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < token.length; i++) {
            map.put(token[i].getKey(), token[i].getValue());
        }
        return initilize(map);
    }

    /**
     * Workaround for kotlin
     *
     * @param content
     */
    public PrintDocument initialize(HashMap<String, Object> content) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        for (Map.Entry<String, Object> test : content.entrySet()) {
            map.put(test.getKey(), test.getValue());
        }
        return initilize(map);
    }

    public PrintDocument initilize(HashMap<String, Object> content) {
        initVelocity();

        /* create a context and add data */
        VelocityContext context = new VelocityContext();

        for (Map.Entry<String, Object> entry : content.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }

        // default date tool
        context.put("date", new DateTool());
        context.put("latexTextConverter", new TextToLatexConverter());
        context.put("histoUtil", new HistoUtil());

        /* now render the template into a StringWriter */
        StringWriter writer = new StringWriter();

        Velocity.evaluate(context, writer, "test", loadedContent);

        finalContent = writer.toString();

        return this;
    }

    public void copyIntoDocument(PrintDocument document) {
        setId(document.getId());
        setName(document.getName());
        setContent(document.getContent());
        setContent2(document.getContent2());
        setType(document.getType());
        setAttributes(document.getAttributes());
        setTemplateName(document.getTemplateName());
        setDefaultOfType(document.isDefaultOfType());
        setTransientContent(document.isTransientContent());
        setDuplexPrinting(document.isDuplexPrinting());
        setPrintEvenPageCount(document.isPrintEvenPageCount());
        setLoadedContent(document.getLoadedContent());
        setUiClass(document.getUiClass());
        setSharedContextGroup(document.getSharedContextGroup());
    }

    public DocumentType getDocumentType() {
        return DocumentType.fromString(this.type);
    }

    public String getGeneratedFileName() {
        return resourceBundle.get("enum.documentType." + getDocumentType()) + "-"
                + TimeUtil.formatDate(new Date(), "dd.MM.yyyy") + ".pdf";
    }

    public String getUiClass() {
        return this.uiClass;
    }

    public int getSharedContextGroup() {
        return this.sharedContextGroup;
    }

    public String getLoadedContent() {
        return this.loadedContent;
    }

    public String getFinalContent() {
        return this.finalContent;
    }

    public boolean isAfterPDFCreationHook() {
        return this.afterPDFCreationHook;
    }

    public boolean isDuplexPrinting() {
        return this.duplexPrinting;
    }

    public boolean isPrintEvenPageCount() {
        return this.printEvenPageCount;
    }

    public void setUiClass(String uiClass) {
        this.uiClass = uiClass;
    }

    public void setSharedContextGroup(int sharedContextGroup) {
        this.sharedContextGroup = sharedContextGroup;
    }

    public void setLoadedContent(String loadedContent) {
        this.loadedContent = loadedContent;
    }

    public void setFinalContent(String finalContent) {
        this.finalContent = finalContent;
    }

    public void setAfterPDFCreationHook(boolean afterPDFCreationHook) {
        this.afterPDFCreationHook = afterPDFCreationHook;
    }

    public void setDuplexPrinting(boolean duplexPrinting) {
        this.duplexPrinting = duplexPrinting;
    }

    public void setPrintEvenPageCount(boolean printEvenPageCount) {
        this.printEvenPageCount = printEvenPageCount;
    }

    /**
     * U_REPORT = Eingangsbogen print at blank page U_REPORT_EMTY = Eingangsbogen
     * print at template, only infill of missing date U_REPORT_COMPLETED = A
     * completed Eingangsbogen with handwriting DIAGNOSIS_REPORT = Report for
     * internal diagnoses, multiple can exist for printing DIAGNOSIS_REPORT_EXTERN =
     * Report for external diagnoses, multiple can exist for printing LABLE = Labels
     * for printing with zebra printers BIOBANK_INFORMED_CONSENT = upload of
     * informed consent for biobank CASE_CONFERENCE = upload and printing of case
     * confernces OTHER
     *
     * @author andi
     */
    public enum DocumentType {
        /**
         * Document with unknown type
         */
        UNKNOWN,

        /**
         * document for printing with patient data an document
         */
        U_REPORT,

        /**
         * document for printing with only patient data, no document data
         */
        U_REPORT_EMTY,

        /**
         * document with filled out fields, this is uploaded by the user
         */
        U_REPORT_COMPLETED,
        /**
         *
         */
        DIAGNOSIS_REPORT, DIAGNOSIS_REPORT_COMPLETED, DIAGNOSIS_REPORT_NOT_APPROVED, DIAGNOSIS_REPORT_EXTERN, LABLE,
        BIOBANK_INFORMED_CONSENT, TEST_LABLE, COUNCIL_REQUEST, COUNCIL_REPLY, OTHER, EMPTY,

        /**
         * Sendreport, generated by the system
         */
        NOTIFICATION_SEND_REPORT,

        /**
         *
         */
        MEDICAL_FINDINGS_SEND_REPORT_COMPLETED,

        /**
         * Document for printing
         */
        PRINT_DOCUMENT;

        public static DocumentType fromString(String text) {
            for (DocumentType b : DocumentType.values()) {
                if (b.name().equalsIgnoreCase(text)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("No constant with text " + text + " found");
        }
    }
}
