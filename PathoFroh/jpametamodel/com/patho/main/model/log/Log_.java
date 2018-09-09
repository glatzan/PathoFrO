package com.patho.main.model.log;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Log.class)
public abstract class Log_ {

	public static volatile SingularAttribute<Log, Integer> id;
	public static volatile SingularAttribute<Log, LogInfo> logInfo;
	public static volatile SingularAttribute<Log, Long> timestamp;

}

