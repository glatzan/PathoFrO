package com.patho.main.model.user;

import com.patho.main.model.Physician;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(HistoUser.class)
public abstract class HistoUser_ {

	public static volatile SingularAttribute<HistoUser, Long> lastLogin;
	public static volatile SingularAttribute<HistoUser, Boolean> archived;
	public static volatile SingularAttribute<HistoUser, HistoSettings> settings;
	public static volatile SingularAttribute<HistoUser, Physician> physician;
	public static volatile SingularAttribute<HistoUser, Long> id;
	public static volatile SingularAttribute<HistoUser, Boolean> localUser;
	public static volatile SingularAttribute<HistoUser, Long> version;
	public static volatile SingularAttribute<HistoUser, String> username;
	public static volatile SingularAttribute<HistoUser, HistoGroup> group;

}

