package com.patho.main.model.interfaces;

import java.util.Set;

import javax.persistence.Transient;

import com.patho.main.model.PDFContainer;
import com.patho.main.template.PrintDocument.DocumentType;

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

	public default boolean contaisReportType(DocumentType type) {
		if (getAttachedPdfs() != null) {
			return getAttachedPdfs().stream().anyMatch(p -> p.getType().equals(type));
		}

		return false;
	}

	/**
	 * Adds a pdf to a datalist
	 * 
	 * @param pdfTemplate
	 */
	public default void addReport(PDFContainer pdfTemplate) {
		getAttachedPdfs().add(pdfTemplate);
	}

	/**
	 * Removes a report
	 * 
	 * @param pdfTemplate
	 */
	public default void removeReport(PDFContainer pdfTemplate) {
		getAttachedPdfs().remove(pdfTemplate);
	}

	/**
	 * Returns true if the pdf is in the datalist
	 * 
	 * @param pdfTemplate
	 * @return
	 */
	public default boolean containsReport(PDFContainer pdfTemplate) {
		return getAttachedPdfs() != null && getAttachedPdfs().contains(pdfTemplate);
	}
}
