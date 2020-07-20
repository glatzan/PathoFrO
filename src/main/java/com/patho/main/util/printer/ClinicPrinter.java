package com.patho.main.util.printer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.transitory.PDFContainerLoaded;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocumentType;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.pdf.PrintOrder;
import com.patho.main.util.pdf.creator.PDFManipulator;
import com.patho.main.util.print.LoadedPrintPDFBearer;
import com.patho.main.util.print.PrintPDFBearer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintJob;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Setter
@Getter
public class ClinicPrinter extends AbstractPrinter {

    private static String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    public ClinicPrinter() {
    }

    public ClinicPrinter(CupsPrinter cupsPrinter) {
        this.id = cupsPrinter.getName().hashCode();
        this.address = cupsPrinter.getPrinterURL().toString();
        this.name = cupsPrinter.getName();
        this.description = cupsPrinter.getDescription();
        this.location = cupsPrinter.getLocation();

        // getting ip
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
//        Matcher matcher = pattern.matcher(cupsPrinter.getDeviceURI());
//
//        if (matcher.find()) {
//            this.deviceUri = matcher.group();
//        } else {
//            this.deviceUri = "0.0.0.0";
//        }
    }

    /**
     * Prints a file
     *
     * @param file
     */
    public boolean print(File file) {
        try {

            PrintJob printJob = new PrintJob.Builder(FileUtils.readFileToByteArray(file)).build();
            logger.debug("Printing file");

            Optional<CupsPrinter> printer = getPrinter();
            if (printer.isPresent()) {
                printer.get().print(printJob);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public boolean print(PDFContainer container) {
        return print(container, "");
    }

    public boolean print(PDFContainer container, int count) {
        return print(container, count, null);
    }

    public boolean print(PDFContainer container, String args) {
        return print(container, 1, args);
    }

    public boolean print(PDFContainer container, PrintDocument template) {
        return print(new PrintOrder(container, template));
    }

    public boolean print(PDFContainer container, int copies, String args) {
        return print(new PrintOrder(container, copies, false, args));
    }

    public boolean print(PrintPDFBearer container, int copies) {
        return print(new PrintOrder(container.getPdfContainer(), copies, container.getPrintDocument()));
    }

    public boolean print(PrintOrder printOrder) {
        logger.debug("Printing xtimes: " + printOrder.getCopies());
        // adding addition
        // al page if duplex print an odd pages are provided
        if (PDFManipulator.countPDFPages((PDFContainerLoaded) printOrder.getPdfContainer().getPdfContainer()) % 2 != 0 && printOrder.isDuplex()) {

            // mergin with empty page
            ArrayList<PDFContainerLoaded> list = new ArrayList<PDFContainerLoaded>();
            list.add((PDFContainerLoaded) printOrder.getPdfContainer().getPdfContainer());
            list.add(new PDFContainerLoaded(new PDFContainer(PrintDocumentType.EMPTY, "",
                    SpringContextBridge.services().getPathoConfig().getDefaultDocuments().getEmptyPage(), "")));

            PDFContainerLoaded result = PDFManipulator.mergeLoadedPDFs(list, false);

            printOrder.setPdfContainer(new LoadedPrintPDFBearer(result, printOrder.getPdfContainer().getPrintDocument()));

            logger.debug("Printing in duplex mode, adding empty page at the end");
        }

        PrintJob printJob = new PrintJob.Builder(new ByteArrayInputStream(((PDFContainerLoaded) printOrder.getPdfContainer().getPdfContainer()).getPdfData()))
                .duplex(printOrder.isDuplex()).copies(printOrder.getCopies()).build();

        if (HistoUtil.isNotNullOrEmpty(printOrder.getArgs())) {

            Map<String, String> attribute = new HashMap<String, String>();
            attribute.put("job-attributes", printOrder.getArgs());

            printJob.setAttributes(attribute);
            logger.debug("Printig with args: " + printOrder.getArgs());
        }

        try {
            Optional<CupsPrinter> printer = getPrinter();
            if (printer.isPresent()) {
                logger.debug("Printer found: " + printer.get().getPrinterURL());
                printer.get().print(printJob);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public boolean printTestPage() {
        return print(new File(SpringContextBridge.services().getPathoConfig().getDefaultDocuments().getCupsPrinterTestPage()));
    }

    /**
     * Creating new cups printer. Storing the printer does not work.
     *
     * @return
     */
    private Optional<CupsPrinter> getPrinter() {
        try {
            CupsClient cupsClient = new CupsClient(SpringContextBridge.services().getPrintService().getCupsPrinter().getHost(),
                    SpringContextBridge.services().getPrintService().getCupsPrinter().getPort());
            CupsPrinter printer = cupsClient.getPrinter(new URL(getAddress()));
            return Optional.ofNullable(printer);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClinicPrinter && ((ClinicPrinter) obj).getId() == getId())
            return true;

        return super.equals(obj);
    }

    public static String printerToJson(ClinicPrinter clinicPrinter) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(clinicPrinter);

    }

    public static ClinicPrinter getPrinterFromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ClinicPrinter.class);
    }
}
