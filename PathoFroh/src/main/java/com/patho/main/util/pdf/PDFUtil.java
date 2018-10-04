package com.patho.main.util.pdf;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.patho.main.model.PDFContainer;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.Task;
import com.patho.main.template.PrintDocument.DocumentType;

public class PDFUtil {

	public static PDFContainer getLastPDFofType(DataList listObj, DocumentType type) {
		List<PDFContainer> resultArr = listObj.getAttachedPdfs().stream().filter(p -> p.getType() == type)
				.collect(Collectors.toList());

		if (resultArr.isEmpty())
			return null;

		PDFContainer newstContainer = resultArr.get(0);

		for (int i = 1; i < resultArr.size(); i++)
			if (resultArr.get(i).getAudit().getCreatedOn() > newstContainer.getAudit().getCreatedOn())
				newstContainer = resultArr.get(i);

		return newstContainer;
	}

	public static Optional<PDFContainer> getDiagnosisReport(Task task, DiagnosisRevision diagnosisRevision) {
		String matcher = PDFContainer.MARKER_DIAGNOSIS.replace("$id", String.valueOf(diagnosisRevision.getId()));
		for (PDFContainer container : task.getAttachedPdfs()) {
			if (container.getIntern() != null && container.getIntern().matches(matcher)) {
				return Optional.ofNullable(container);
			}
		}
		return Optional.empty();
	}
	
	public static List<PDFContainer> getPDFsofType(List<PDFContainer> containers, DocumentType type) {
		return containers.stream().filter(p -> p.getType().equals(type)).collect(Collectors.toList());
	}

	public static PDFContainer getLatestPDFofType(List<PDFContainer> containers, DocumentType type) {
		return getLatestPDFofType(getPDFsofType(containers, type));
	}

	// TODO remove 
	public static PDFContainer getLatestPDFofType(List<PDFContainer> containers) {
		if (containers.size() == 0)
			return null;

		PDFContainer latest = containers.get(0);

		for (PDFContainer pdfContainer : containers) {
			if (latest.getAudit().getCreatedOn() < pdfContainer.getAudit().getCreatedOn())
				latest = pdfContainer;
		}

		return latest;
	}

	public static List<PDFContainer> sortPDFListByDate(List<PDFContainer> list, boolean asc) {

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

		return null;
	}
}
