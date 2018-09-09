package com.patho.main.model.favourites;

import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(FavouriteListItem.class)
public abstract class FavouriteListItem_ {

	public static volatile SingularAttribute<FavouriteListItem, FavouriteList> favouriteList;
	public static volatile ListAttribute<FavouriteListItem, Slide> slides;
	public static volatile SingularAttribute<FavouriteListItem, Task> task;
	public static volatile SingularAttribute<FavouriteListItem, Long> id;
	public static volatile SingularAttribute<FavouriteListItem, String> commentary;

}

