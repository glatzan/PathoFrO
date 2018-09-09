package com.patho.main.model.user;

import com.patho.main.model.user.HistoGroup.AuthRole;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(HistoGroup.class)
public abstract class HistoGroup_ {

	public static volatile SingularAttribute<HistoGroup, HistoSettings> settings;
	public static volatile SingularAttribute<HistoGroup, Boolean> archived;
	public static volatile SingularAttribute<HistoGroup, AuthRole> authRole;
	public static volatile SetAttribute<HistoGroup, HistoPermissions> permissions;
	public static volatile SingularAttribute<HistoGroup, String> name;
	public static volatile SingularAttribute<HistoGroup, Long> id;
	public static volatile SingularAttribute<HistoGroup, Boolean> userDeactivated;
	public static volatile SingularAttribute<HistoGroup, Long> version;
	public static volatile SingularAttribute<HistoGroup, String> commentary;

}

