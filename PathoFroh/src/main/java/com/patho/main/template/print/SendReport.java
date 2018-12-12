package com.patho.main.template.print;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.notification.NotificationContainerList;
import com.patho.main.util.pdf.PDFCreator;
import com.patho.main.util.printer.LoadedPDFContainer;

/**
 * patient, <br>
 * task, <br>
 * temporarayNotification(not final), <br>
 * useMail, <br>
 * mailHolders, <br>
 * useFax, <br>
 * faxContainer, <br>
 * useLetter, <br>
 * letterContainer, <br>
 * usePhone,<br>
 * phoneContainer, <br>
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

	public PDFContainer onAfterPDFCreation(PDFContainer container, PDFCreator creator) {
		List<LoadedPDFContainer> attachPdf = new ArrayList<LoadedPDFContainer>();

		attachPdf.add(new LoadedPDFContainer(container));

		attachPdf.addAll(faxContainerList.getContainerToNotify().stream().filter(p -> p.getPdf() != null)
				.map(p -> new LoadedPDFContainer(p.getPdf())).collect(Collectors.toList()));
		attachPdf.addAll(letterContainerList.getContainerToNotify().stream().filter(p -> p.getPdf() != null)
				.map(p -> new LoadedPDFContainer(p.getPdf())).collect(Collectors.toList()));
		attachPdf.addAll(phoneContainerList.getContainerToNotify().stream().filter(p -> p.getPdf() != null)
				.map(p -> new LoadedPDFContainer(p.getPdf())).collect(Collectors.toList()));

		container = creator.mergePDFs(container, attachPdf);

		return container;
	}
}
