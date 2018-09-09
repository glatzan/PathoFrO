package com.patho.main.model.patient;

import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.model.Signature;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(DiagnosisRevision.class)
public abstract class DiagnosisRevision_ {

	public static volatile SingularAttribute<DiagnosisRevision, Task> parent;
	public static volatile SingularAttribute<DiagnosisRevision, Signature> signatureTwo;
	public static volatile SingularAttribute<DiagnosisRevision, DiagnosisRevisionType> type;
	public static volatile SingularAttribute<DiagnosisRevision, Long> creationDate;
	public static volatile SingularAttribute<DiagnosisRevision, Long> version;
	public static volatile ListAttribute<DiagnosisRevision, Diagnosis> diagnoses;
	public static volatile SingularAttribute<DiagnosisRevision, Long> signatureDate;
	public static volatile SingularAttribute<DiagnosisRevision, Boolean> notificationPending;
	public static volatile SingularAttribute<DiagnosisRevision, String> name;
	public static volatile SingularAttribute<DiagnosisRevision, Long> completionDate;
	public static volatile SingularAttribute<DiagnosisRevision, Long> id;
	public static volatile SingularAttribute<DiagnosisRevision, String> text;
	public static volatile SingularAttribute<DiagnosisRevision, Signature> signatureOne;
	public static volatile SingularAttribute<DiagnosisRevision, Long> notificationDate;

}

