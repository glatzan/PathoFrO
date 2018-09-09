package com.patho.main.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Signature.class)
public abstract class Signature_ {

	public static volatile SingularAttribute<Signature, String> role;
	public static volatile SingularAttribute<Signature, Physician> physician;
	public static volatile SingularAttribute<Signature, Long> id;
	public static volatile SingularAttribute<Signature, Long> version;

}

