package com.patho.main.repository;

import java.util.List;
import java.util.Optional;

import com.patho.main.template.PrintDocument;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.util.helper.StreamUtils;

public interface PrintDocumentRepository {

	List<PrintDocument> findAllByTypes(DocumentType... types);

	List<PrintDocument> findAllByTypes(List<DocumentType> types);

	List<PrintDocument> findAll();

	Optional<PrintDocument> findByID(long id);

	Optional<PrintDocument> findByTypeAndDefault(DocumentType type);

	/**
	 * Returns the default document of a given type.
	 * 
	 * @param documents
	 * @param type
	 * @return
	 */
	public static Optional<PrintDocument> getByTypAndDefault(List<PrintDocument> documents, DocumentType type) {

		for (PrintDocument printDocument : documents) {
			if (printDocument.getDocumentType() == type && printDocument.isDefaultOfType())
				return Optional.ofNullable(printDocument);
		}
		return Optional.empty();
	}
}