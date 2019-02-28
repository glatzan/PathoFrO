package com.patho.main.util.printer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.patho.main.model.PDFContainer;
import com.patho.main.repository.MediaRepository;
import com.patho.main.service.PrintService;
import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.pdf.PDFCreator;
import com.patho.main.util.pdf.PrintOrder;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configurable
public class ClinicPrinter extends AbstractPrinter {

	private static String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

	@Autowired
	protected PathoConfig pathoConfig;

	@Autowired
	protected MediaRepository mediaRepository;

	@Autowired
	protected PrintService printService;

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
		Matcher matcher = pattern.matcher(cupsPrinter.getDeviceURI());

		if (matcher.find()) {
			this.deviceUri = matcher.group();
		} else {
			this.deviceUri = "0.0.0.0";
		}
	}

	/**
	 * Prints a file
	 * 
	 * @param cPrinter
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

	public boolean print(PrintOrder printOrder) {
		logger.debug("Printing xtimes: " + printOrder.getCopies());

		// adding additional page if duplex print an odd pages are provided
		if (PDFCreator.countPDFPages(printOrder.getPdfContainer()) % 2 != 0 && printOrder.isDuplex()) {

			// Merging with empty page
			LoadedPDFContainer cont = PDFCreator.mergePdfs(
					Arrays.asList(printOrder.getPdfContainer(),
							new LoadedPDFContainer(new PDFContainer(DocumentType.EMPTY,
									pathoConfig.getDefaultDocuments().getEmptyPage()))),
					"", DocumentType.PRINT_DOCUMENT);

			printOrder.setPdfContainer(cont);

			logger.debug("Printing in duplex mode, adding empty page at the end");
		}

		PrintJob printJob = new PrintJob.Builder(new ByteArrayInputStream(printOrder.getPdfContainer().getPdfData()))
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
		return print(new File(pathoConfig.getDefaultDocuments().getCupsPrinterTestPage()));
	}

	/**
	 * Creating new cups printer. Storing the printer does not work.
	 * 
	 * @return
	 */
	private Optional<CupsPrinter> getPrinter() {
		try {
			CupsClient cupsClient = new CupsClient(printService.getCupsPrinter().getHost(),
					printService.getCupsPrinter().getPort());
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
