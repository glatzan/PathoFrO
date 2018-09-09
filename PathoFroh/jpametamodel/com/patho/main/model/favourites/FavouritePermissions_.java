package com.patho.main.model.favourites;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(FavouritePermissions.class)
public abstract class FavouritePermissions_ {

	public static volatile SingularAttribute<FavouritePermissions, Boolean> readable;
	public static volatile SingularAttribute<FavouritePermissions, FavouriteList> favouriteList;
	public static volatile SingularAttribute<FavouritePermissions, Boolean> editable;
	public static volatile SingularAttribute<FavouritePermissions, Boolean> admin;
	public static volatile SingularAttribute<FavouritePermissions, Long> id;

}

