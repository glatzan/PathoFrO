package com.patho.main.model;

import com.patho.main.model.ListItem.StaticList;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ListItem.class)
public abstract class ListItem_ {

	public static volatile SingularAttribute<ListItem, Boolean> archived;
	public static volatile SingularAttribute<ListItem, Integer> indexInList;
	public static volatile SingularAttribute<ListItem, Long> id;
	public static volatile SingularAttribute<ListItem, StaticList> listType;
	public static volatile SingularAttribute<ListItem, String> value;

}

