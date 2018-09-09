package com.patho.main.model.patient;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Block.class)
public abstract class Block_ {

	public static volatile SingularAttribute<Block, String> blockID;
	public static volatile SingularAttribute<Block, Sample> parent;
	public static volatile ListAttribute<Block, Slide> slides;
	public static volatile SingularAttribute<Block, Boolean> idManuallyAltered;
	public static volatile SingularAttribute<Block, Long> id;
	public static volatile SingularAttribute<Block, Long> creationDate;
	public static volatile SingularAttribute<Block, Long> version;

}

