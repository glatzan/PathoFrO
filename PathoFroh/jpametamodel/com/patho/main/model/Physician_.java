package com.patho.main.model;

import com.patho.main.common.ContactRole;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Physician.class)
public abstract class Physician_ {

	public static volatile SingularAttribute<Physician, String> uid;
	public static volatile SingularAttribute<Physician, Boolean> archived;
	public static volatile SingularAttribute<Physician, Integer> priorityCount;
	public static volatile SingularAttribute<Physician, Person> person;
	public static volatile SetAttribute<Physician, ContactRole> associatedRoles;
	public static volatile SingularAttribute<Physician, Long> id;
	public static volatile SingularAttribute<Physician, String> clinicRole;
	public static volatile SingularAttribute<Physician, Long> version;
	public static volatile SingularAttribute<Physician, String> employeeNumber;

}

