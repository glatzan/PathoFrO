package com.patho.main.model.patient;

import com.patho.main.model.PDFContainer;
import com.patho.main.model.Person;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Patient.class)
public abstract class Patient_ {

	public static volatile SingularAttribute<Patient, String> piz;
	public static volatile SingularAttribute<Patient, String> insurance;
	public static volatile SingularAttribute<Patient, Boolean> archived;
	public static volatile SingularAttribute<Patient, Person> person;
	public static volatile SingularAttribute<Patient, Boolean> externalPatient;
	public static volatile SetAttribute<Patient, PDFContainer> attachedPdfs;
	public static volatile SingularAttribute<Patient, Long> id;
	public static volatile SingularAttribute<Patient, Long> creationDate;
	public static volatile SingularAttribute<Patient, Long> version;
	public static volatile SetAttribute<Patient, Task> tasks;

}

