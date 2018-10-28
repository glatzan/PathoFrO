package com.patho.main.template.print;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.notification.NotificationContainerList;
import com.patho.main.util.pdf.PDFGenerator;

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

	public void initilize(HashMap<String, Object> content) {
		super.initilize(content);
		faxContainerList = (NotificationContainerList) content.get("faxContainer");
		letterContainerList = (NotificationContainerList) content.get("letterContainer");
		phoneContainerList = (NotificationContainerList) content.get("phoneContainer");
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

		return PDFGenerator.mergePdfs(attachPdf, "Send Report", DocumentType.MEDICAL_FINDINGS_SEND_REPORT_COMPLETED);
	}
}
