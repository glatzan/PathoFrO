package com.patho.main.template.print;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.InitializeToken;
import com.patho.main.template.PrintDocument;
import com.patho.main.util.helper.HistoUtil;
import com.patho.main.util.notification.NotificationContainer;
import com.patho.main.util.pdf.PDFCreator;
import com.patho.main.util.print.LoadedPrintPDFBearer;

/**
 * patient, <br>
 * task, <br>
 * diagnosisRevisions. <br>
 * useMail, <br>
 * mails, <br>
 * useFax, <br>
 * faxes, <br>
 * useLetter, <br>
 * letters, <br>
 * usePhone,<br>
 * phonenumbers, <br>
 * reportDate
 * 
 * @author andi
 *
 */
public class SendReport extends PrintDocument {

	private boolean useMail;
	private List<NotificationContainer> mails;

	private boolean useFax;
	private List<NotificationContainer> faxes;

	private boolean useLetters;
	private List<NotificationContainer> letters;

	private boolean usePhone;
	private List<NotificationContainer> phonenumbers;

	public SendReport(PrintDocument document) {
		super(document);
		setAfterPDFCreationHook(true);
	}

	public void initilize(InitializeToken... token) {

		for (InitializeToken initializeToken : token) {
			switch (initializeToken.getKey()) {
			case "useMail":
				useMail = (boolean) initializeToken.getValue();
				break;
			case "mails":
				mails = (List<NotificationContainer>) initializeToken.getValue();
				break;
			case "useFax":
				useFax = (boolean) initializeToken.getValue();
				break;
			case "faxes":
				faxes = (List<NotificationContainer>) initializeToken.getValue();
				break;
			case "useLetter":
				useLetters = (boolean) initializeToken.getValue();
				break;
			case "letters":
				letters = (List<NotificationContainer>) initializeToken.getValue();
				break;
			case "usePhone":
				usePhone = (boolean) initializeToken.getValue();
				break;
			case "phonenumbers":
				phonenumbers = (List<NotificationContainer>) initializeToken.getValue();
				break;
			default:
				break;
			}
		}

		HashMap<String, Object> map = new HashMap<String, Object>();

		for (int i = 0; i < token.length; i++) {
			map.put(token[i].getKey(), token[i].getValue());
		}
		initilize(map);
	}

	public PDFContainer onAfterPDFCreation(PDFContainer container, PDFCreator creator) {
		List<LoadedPrintPDFBearer> attachPdf = new ArrayList<LoadedPrintPDFBearer>();

		attachPdf.add(new LoadedPrintPDFBearer(container));

//		if (useFax && HistoUtil.isNotNullOrEmpty(mails))
//			attachPdf.addAll(mails.stream().filter(p -> p.getPdf() != null).map(p -> new LoadedPrintPDFBearer(p))
//					.collect(Collectors.toList()));
//
//		if (useFax && HistoUtil.isNotNullOrEmpty(faxes))
//			attachPdf.addAll(faxes.stream().filter(p -> p.getPdf() != null).map(p -> new LoadedPrintPDFBearer(p.getPdf()))
//					.collect(Collectors.toList()));
//
//		if (useLetters && HistoUtil.isNotNullOrEmpty(letters))
//			attachPdf.addAll(letters.stream().filter(p -> p.getPdf() != null)
//					.map(p -> new LoadedPrintPDFBearer(p.getPdf())).collect(Collectors.toList()));
//
//		if (usePhone && HistoUtil.isNotNullOrEmpty(phonenumbers))
//			attachPdf.addAll(phonenumbers.stream().filter(p -> p.getPdf() != null)
//					.map(p -> new LoadedPrintPDFBearer(p.getPdf())).collect(Collectors.toList()));

		container = creator.mergePDFs(container, attachPdf);

		return container;
	}
}
