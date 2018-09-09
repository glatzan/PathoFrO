package com.patho.main.model;

import com.patho.main.common.InformedConsentType;
import com.patho.main.model.patient.Task;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BioBank.class)
public abstract class BioBank_ {

	public static volatile SingularAttribute<BioBank, Task> task;
	public static volatile SetAttribute<BioBank, PDFContainer> attachedPdfs;
	public static volatile SingularAttribute<BioBank, Long> id;
	public static volatile SingularAttribute<BioBank, InformedConsentType> informedConsentType;
	public static volatile SingularAttribute<BioBank, Long> version;

}

