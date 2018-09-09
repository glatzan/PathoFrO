package com.patho.main.model.patient;

import com.patho.main.model.MaterialPreset;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Sample.class)
public abstract class Sample_ {

	public static volatile SingularAttribute<Sample, Task> parent;
	public static volatile SingularAttribute<Sample, String> material;
	public static volatile SingularAttribute<Sample, String> sampleID;
	public static volatile ListAttribute<Sample, Block> blocks;
	public static volatile SingularAttribute<Sample, MaterialPreset> materialPreset;
	public static volatile SingularAttribute<Sample, Boolean> idManuallyAltered;
	public static volatile SingularAttribute<Sample, Boolean> reStainingPhase;
	public static volatile SingularAttribute<Sample, Long> id;
	public static volatile SingularAttribute<Sample, Long> creationDate;
	public static volatile SingularAttribute<Sample, Long> version;

}

