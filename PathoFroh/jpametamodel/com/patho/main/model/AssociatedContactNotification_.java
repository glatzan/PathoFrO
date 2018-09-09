package com.patho.main.model;

import com.patho.main.model.AssociatedContactNotification.NotificationTyp;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AssociatedContactNotification.class)
public abstract class AssociatedContactNotification_ {

	public static volatile SingularAttribute<AssociatedContactNotification, Boolean> renewed;
	public static volatile SingularAttribute<AssociatedContactNotification, NotificationTyp> notificationTyp;
	public static volatile SingularAttribute<AssociatedContactNotification, AssociatedContact> contact;
	public static volatile SingularAttribute<AssociatedContactNotification, Boolean> manuallyAdded;
	public static volatile SingularAttribute<AssociatedContactNotification, Boolean> active;
	public static volatile SingularAttribute<AssociatedContactNotification, String> contactAddress;
	public static volatile SingularAttribute<AssociatedContactNotification, Long> id;
	public static volatile SingularAttribute<AssociatedContactNotification, Boolean> failed;
	public static volatile SingularAttribute<AssociatedContactNotification, Date> dateOfAction;
	public static volatile SingularAttribute<AssociatedContactNotification, Boolean> performed;
	public static volatile SingularAttribute<AssociatedContactNotification, String> commentary;

}

