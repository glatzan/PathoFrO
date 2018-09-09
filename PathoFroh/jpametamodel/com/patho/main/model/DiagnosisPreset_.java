package com.patho.main.model;

import com.patho.main.common.ContactRole;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(DiagnosisPreset.class)
public abstract class DiagnosisPreset_ {

	public static volatile SingularAttribute<DiagnosisPreset, Boolean> malign;
	public static volatile SetAttribute<DiagnosisPreset, ContactRole> diagnosisReportAsLetter;
	public static volatile SingularAttribute<DiagnosisPreset, String> extendedDiagnosisText;
	public static volatile SingularAttribute<DiagnosisPreset, Integer> indexInList;
	public static volatile SingularAttribute<DiagnosisPreset, String> diagnosis;
	public static volatile SingularAttribute<DiagnosisPreset, Long> id;
	public static volatile SingularAttribute<DiagnosisPreset, String> category;
	public static volatile SingularAttribute<DiagnosisPreset, String> commentary;
	public static volatile SingularAttribute<DiagnosisPreset, String> icd10;

}

