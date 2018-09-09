package com.patho.main.model.favourites;

import com.patho.main.model.user.HistoUser;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(FavouriteList.class)
public abstract class FavouriteList_ {

	public static volatile SingularAttribute<FavouriteList, HistoUser> owner;
	public static volatile SingularAttribute<FavouriteList, String> infoText;
	public static volatile SingularAttribute<FavouriteList, Boolean> hideList;
	public static volatile SetAttribute<FavouriteList, HistoUser> hideListForUser;
	public static volatile SingularAttribute<FavouriteList, Boolean> useIcon;
	public static volatile SingularAttribute<FavouriteList, String> icon;
	public static volatile SetAttribute<FavouriteList, FavouritePermissionsGroup> groups;
	public static volatile SingularAttribute<FavouriteList, String> command;
	public static volatile SetAttribute<FavouriteList, FavouritePermissionsUser> users;
	public static volatile SingularAttribute<FavouriteList, Boolean> defaultList;
	public static volatile SingularAttribute<FavouriteList, FavouriteList> dumpList;
	public static volatile SingularAttribute<FavouriteList, Boolean> useDumplist;
	public static volatile SingularAttribute<FavouriteList, Boolean> manuelEdit;
	public static volatile SingularAttribute<FavouriteList, String> name;
	public static volatile SingularAttribute<FavouriteList, String> iconColor;
	public static volatile SingularAttribute<FavouriteList, Boolean> globalView;
	public static volatile SingularAttribute<FavouriteList, Long> id;
	public static volatile ListAttribute<FavouriteList, FavouriteListItem> items;
	public static volatile SingularAttribute<FavouriteList, String> dumpCommentary;
	public static volatile SingularAttribute<FavouriteList, String> commentary;

}

