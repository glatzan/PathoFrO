package com.patho.main.model;

import com.patho.main.model.util.audit.Audit;
import com.patho.main.template.PrintDocument.DocumentType;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PDFContainer.class)
public abstract class PDFContainer_ {

	public static volatile SingularAttribute<PDFContainer, Boolean> finalDocument;
	public static volatile SingularAttribute<PDFContainer, String> path;
	public static volatile SingularAttribute<PDFContainer, String> thumbnail;
	public static volatile SingularAttribute<PDFContainer, String> intern;
	public static volatile SingularAttribute<PDFContainer, byte[]> data;
	public static volatile SingularAttribute<PDFContainer, Audit> audit;
	public static volatile SingularAttribute<PDFContainer, Boolean> restricted;
	public static volatile SingularAttribute<PDFContainer, String> name;
	public static volatile SingularAttribute<PDFContainer, Long> id;
	public static volatile SingularAttribute<PDFContainer, DocumentType> type;
	public static volatile SingularAttribute<PDFContainer, String> commentary;

}

