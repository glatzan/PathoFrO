package com.patho.main.model.interfaces;

import java.util.Set;

import javax.persistence.Transient;

import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.model.PDFContainer;

/**
 * Classes have a data list
 * 
 * @author andi
 *
 */
public interface DataList extends ID {
	
	public Set<PDFContainer> getAttachedPdfs();

	public void setAttachedPdfs(Set<PDFContainer> attachedPdfs);

	public String getPublicName();

	public default boolean contaisPDFofType(DocumentType type) {
		if (getAttachedPdfs() != null) {
			return getAttachedPdfs().stream().anyMatch(p -> p.getType().equals(type));
		}

		return false;
	}

	public default void addReport(PDFContainer pdfTemplate) {
		getAttachedPdfs().add(pdfTemplate);
	}

	/**
	 * Removes a report with a specific type from the database
	 * 
	 * @param type
	 * @return
	 */
	@Transient
	public default PDFContainer removeReport(DocumentType type) {
		for (PDFContainer pdfContainer : getAttachedPdfs()) {
			if (pdfContainer.getType().equals(type)) {
				getAttachedPdfs().remove(pdfContainer);
				return pdfContainer;
			}
		}
		return null;
	}
}
