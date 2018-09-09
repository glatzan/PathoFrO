package com.patho.main.model;

import com.patho.main.model.Person.Gender;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Person.class)
public abstract class Person_ {

	public static volatile SingularAttribute<Person, Date> birthday;
	public static volatile SingularAttribute<Person, String> lastName;
	public static volatile SingularAttribute<Person, String> note;
	public static volatile SingularAttribute<Person, Boolean> autoUpdate;
	public static volatile SingularAttribute<Person, Gender> gender;
	public static volatile SingularAttribute<Person, String> language;
	public static volatile SingularAttribute<Person, String> title;
	public static volatile SingularAttribute<Person, Long> version;
	public static volatile SingularAttribute<Person, String> birthName;
	public static volatile SingularAttribute<Person, String> firstName;
	public static volatile SingularAttribute<Person, Boolean> archived;
	public static volatile ListAttribute<Person, Organization> organizsations;
	public static volatile SingularAttribute<Person, Contact> contact;
	public static volatile SingularAttribute<Person, Long> id;
	public static volatile SingularAttribute<Person, Organization> defaultAddress;

}

