package com.patho.main.model.util.audit;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Audit.class)
public abstract class Audit_ {

	public static volatile SingularAttribute<Audit, String> updatedBy;
	public static volatile SingularAttribute<Audit, String> createdBy;
	public static volatile SingularAttribute<Audit, Long> updatedOn;
	public static volatile SingularAttribute<Audit, Long> createdOn;

}

