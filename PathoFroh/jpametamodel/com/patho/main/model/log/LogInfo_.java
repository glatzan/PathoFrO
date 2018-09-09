package com.patho.main.model.log;

import com.patho.main.model.patient.Patient;
import com.patho.main.model.user.HistoUser;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LogInfo.class)
public abstract class LogInfo_ {

	public static volatile SingularAttribute<LogInfo, HistoUser> histoUser;
	public static volatile SingularAttribute<LogInfo, String> test;
	public static volatile SingularAttribute<LogInfo, Patient> patient;
	public static volatile SingularAttribute<LogInfo, String> logString;

}

