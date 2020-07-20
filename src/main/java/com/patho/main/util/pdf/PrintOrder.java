package com.patho.main.util.pdf;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.print.LoadedPrintPDFBearer;
import lombok.Getter;
import lombok.Setter;

/**
 * Is returned by template in order to print.
 *
 * @author andi
 */
@Getter
@Setter
public class PrintOrder {

    private LoadedPrintPDFBearer pdfContainer;
    private int copies;
    private boolean duplex;
    private boolean printEvenPageCount;
    private String args;

    public PrintOrder(PDFContainer container) {
        this(container, 1, null);
    }

    public PrintOrder(PDFContainer container, PrintDocument documentTemplate) {
        this(container, 1, documentTemplate, false);
    }

    public PrintOrder(PDFContainer container, int copies, PrintDocument documentTemplate) {
        this(container, copies, documentTemplate, false);
    }

    public PrintOrder(PDFContainer container, int copies, PrintDocument documentTemplate, boolean duplex) {
        this(container, copies, documentTemplate.getDuplexPrinting() || duplex, documentTemplate.getAttributes());
    }

    public PrintOrder(PDFContainer container, int copies, boolean duplex, String args) {
        this.pdfContainer = new LoadedPrintPDFBearer(container);
        this.duplex = duplex;
        this.args = args;
        this.copies = copies;
    }
}
