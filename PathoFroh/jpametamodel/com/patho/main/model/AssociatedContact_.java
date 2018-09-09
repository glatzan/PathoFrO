package com.patho.main.model;

import com.patho.main.common.ContactRole;
import com.patho.main.model.patient.Task;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AssociatedContact.class)
public abstract class AssociatedContact_ {

	public static volatile SingularAttribute<AssociatedContact, Task> task;
	public static volatile SingularAttribute<AssociatedContact, ContactRole> role;
	public static volatile SingularAttribute<AssociatedContact, Person> person;
	public static volatile SingularAttribute<AssociatedContact, Long> id;
	public static volatile ListAttribute<AssociatedContact, AssociatedContactNotification> notifications;

}

