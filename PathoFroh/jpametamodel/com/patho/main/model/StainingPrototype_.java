package com.patho.main.model;

import com.patho.main.model.StainingPrototype.StainingType;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(StainingPrototype.class)
public abstract class StainingPrototype_ {

	public static volatile SingularAttribute<StainingPrototype, Boolean> archived;
	public static volatile SingularAttribute<StainingPrototype, String> name;
	public static volatile SingularAttribute<StainingPrototype, Integer> indexInList;
	public static volatile ListAttribute<StainingPrototype, StainingPrototypeDetails> batchDetails;
	public static volatile SingularAttribute<StainingPrototype, Long> id;
	public static volatile SingularAttribute<StainingPrototype, StainingType> type;
	public static volatile SingularAttribute<StainingPrototype, String> commentary;

}

