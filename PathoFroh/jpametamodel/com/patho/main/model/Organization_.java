package com.patho.main.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Organization.class)
public abstract class Organization_ {

	public static volatile SingularAttribute<Organization, String> note;
	public static volatile ListAttribute<Organization, Person> persons;
	public static volatile SingularAttribute<Organization, Boolean> archived;
	public static volatile SingularAttribute<Organization, Boolean> intern;
	public static volatile SingularAttribute<Organization, Contact> contact;
	public static volatile SingularAttribute<Organization, String> name;
	public static volatile SingularAttribute<Organization, Long> id;
	public static volatile SingularAttribute<Organization, Long> version;

}

