package com.patho.main.model.patient;

import com.patho.main.model.DiagnosisPreset;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Diagnosis.class)
public abstract class Diagnosis_ {

	public static volatile SingularAttribute<Diagnosis, DiagnosisRevision> parent;
	public static volatile SingularAttribute<Diagnosis, Boolean> malign;
	public static volatile SingularAttribute<Diagnosis, String> name;
	public static volatile SingularAttribute<Diagnosis, String> diagnosis;
	public static volatile SingularAttribute<Diagnosis, DiagnosisPreset> diagnosisPrototype;
	public static volatile SingularAttribute<Diagnosis, Long> id;
	public static volatile SingularAttribute<Diagnosis, Long> version;
	public static volatile SingularAttribute<Diagnosis, Sample> sample;
	public static volatile SingularAttribute<Diagnosis, String> icd10;

}

