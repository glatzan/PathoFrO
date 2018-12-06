package com.patho.main.template.print;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.notification.NotificationContainerList;
import com.patho.main.util.pdf.PDFGenerator;
import com.patho.main.util.printer.LoadedPDFContainer;

/**
 * patient, task, temporarayNotification(not final) , useMail, mailHolders,
 * useFax, faxContainer, useLetter, letterContainer, usePhone,phoneContainer,
 * reportDate
 * 
 * @author andi
 *
 */
public class SendReport extends PrintDocument {

	private NotificationContainerList faxContainerList;
	private NotificationContainerList letterContainerList;
	private NotificationContainerList phoneContainerList;

	public SendReport(PrintDocument document) {
		super(document);
		setAfterPDFCreationHook(true);
	}

	public PDFContainer onAfterPDFCreation(PDFContainer container) {
		List<PDFContainer> attachPdf = new ArrayList<PDFContainer>();

		attachPdf.add(container);

		attachPdf.addAll(faxContainerList.getContainerToNotify().stream().filter(p -> p.getPdf() != null)
				.map(p -> p.getPdf()).collect(Collectors.toList()));
		attachPdf.addAll(letterContainerList.getContainerToNotify().stream().filter(p -> p.getPdf() != null)
				.map(p -> p.getPdf()).collect(Collectors.toList()));
		attachPdf.addAll(phoneContainerList.getContainerToNotify().stream().filter(p -> p.getPdf() != null)
				.map(p -> p.getPdf()).collect(Collectors.toList()));

		LoadedPDFContainer loadedContainer = PDFGenerator.mergePdfs(
				attachPdf.stream().map(p -> new LoadedPDFContainer(p)).collect(Collectors.toList()), "Send Report",
				DocumentType.MEDICAL_FINDINGS_SEND_REPORT_COMPLETED);

		return null;
	}
}
