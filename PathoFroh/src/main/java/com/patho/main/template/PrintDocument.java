package com.patho.main.template;

import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.contentstream.operator.state.SetGraphicsStateParameters;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.generic.DateTool;

import com.patho.main.action.handler.GlobalSettings;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.template.print.ui.AbstractDocumentUi;
import com.patho.main.util.helper.FileUtil;
import com.patho.main.util.helper.StreamUtils;
import com.patho.main.util.helper.TextToLatexConverter;
import com.patho.main.util.pdf.PDFGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
public class PrintDocument extends Template {

	/**
	 * Raw file content from hdd
	 */
	private String loadedContent;

	/**
	 * Processed final document 
	 */
	private String finalContent;
	
	/**
	 * If true the pdf generator will call onAfterPDFCreation to allow the template
	 * to change or attach other things to the pdf
	 */
	protected boolean afterPDFCreationHook;

	/**
	 * Document copies
	 */
	protected int copies = 1;

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
	
	public PrintDocument(PrintDocument document) {
		copyIntoDocument(document);
	}

	/**
	 * Is called if afterPDFCreationHook is set and the pdf was created.
	 * 
	 * @param container
	 * @return
	 */
	public PDFContainer onAfterPDFCreation(PDFContainer container) {
		return container;
	}

	public void initilize(InitializeToken... token) {
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		for (int i = 0; i < token.length; i++) {
			map.put(token[i].getKey(), token[i].getValue());
		}
		initilize(map);
	}
	
	public void initilize(HashMap<String, Object> content) {
		initVelocity();

		/* create a context and add data */
		VelocityContext context = new VelocityContext();

		for(Map.Entry<String, Object> entry : content.entrySet()) { 
			context.put(entry.getKey(), entry.getValue());
		}
		
		// default date tool
		context.put("date", new DateTool());
		context.put("latexTextConverter", new TextToLatexConverter());
		
		/* now render the template into a StringWriter */
		StringWriter writer = new StringWriter();

		Velocity.evaluate(context, writer, "test", loadedContent);
		
		finalContent = writer.toString();
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
	}
	
	public DocumentType getDocumentType() {
		return DocumentType.fromString(this.type);
	}

	public AbstractDocumentUi<?> getDocumentUi() {
		return new AbstractDocumentUi(this);
	}
	
	public String getGeneratedFileName() {
		return "GenericFile.pdf";
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
		 * DIAGNOSIS_REPORT
		 */
		U_REPORT, U_REPORT_EMTY, U_REPORT_COMPLETED, DIAGNOSIS_REPORT, DIAGNOSIS_REPORT_COMPLETED, DIAGNOSIS_REPORT_NOT_APPROVED, DIAGNOSIS_REPORT_EXTERN, LABLE, BIOBANK_INFORMED_CONSENT, TEST_LABLE, COUNCIL_REQUEST, COUNCIL_REPLY, OTHER, EMPTY, NOTIFICATION_SEND_REPORT, MEDICAL_FINDINGS_SEND_REPORT_COMPLETED,

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

	
	@Getter
	@Setter
	@AllArgsConstructor
	public static class InitializeToken{
		private String key;
		private Object value;
	}
}
