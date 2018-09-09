package com.patho.main.model.patient;

import com.patho.main.model.StainingPrototype;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Slide.class)
public abstract class Slide_ {

	public static volatile SingularAttribute<Slide, Block> parent;
	public static volatile SingularAttribute<Slide, String> slideID;
	public static volatile SingularAttribute<Slide, StainingPrototype> slidePrototype;
	public static volatile SingularAttribute<Slide, Boolean> idManuallyAltered;
	public static volatile SingularAttribute<Slide, Boolean> reStaining;
	public static volatile SingularAttribute<Slide, Long> completionDate;
	public static volatile SingularAttribute<Slide, Long> id;
	public static volatile SingularAttribute<Slide, Integer> uniqueIDinTask;
	public static volatile SingularAttribute<Slide, Long> creationDate;
	public static volatile SingularAttribute<Slide, Long> version;
	public static volatile SingularAttribute<Slide, Boolean> stainingCompleted;
	public static volatile SingularAttribute<Slide, String> commentary;

}

