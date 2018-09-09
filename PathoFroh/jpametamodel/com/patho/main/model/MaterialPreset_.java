package com.patho.main.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(MaterialPreset.class)
public abstract class MaterialPreset_ {

	public static volatile ListAttribute<MaterialPreset, StainingPrototype> stainingPrototypes;
	public static volatile SingularAttribute<MaterialPreset, String> name;
	public static volatile SingularAttribute<MaterialPreset, Integer> indexInList;
	public static volatile SingularAttribute<MaterialPreset, Long> id;
	public static volatile SingularAttribute<MaterialPreset, String> commentary;

}

