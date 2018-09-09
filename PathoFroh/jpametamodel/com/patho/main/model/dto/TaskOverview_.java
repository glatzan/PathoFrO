package com.patho.main.model.dto;

import com.patho.main.common.TaskPriority;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TaskOverview.class)
public abstract class TaskOverview_ {

	public static volatile SingularAttribute<TaskOverview, Date> birthday;
	public static volatile SingularAttribute<TaskOverview, String> lastName;
	public static volatile SingularAttribute<TaskOverview, Boolean> diagnosisCompleted;
	public static volatile SingularAttribute<TaskOverview, TaskPriority> taskPriority;
	public static volatile SingularAttribute<TaskOverview, String> title;
	public static volatile SingularAttribute<TaskOverview, Boolean> userContact;
	public static volatile SingularAttribute<TaskOverview, Boolean> finalized;
	public static volatile SingularAttribute<TaskOverview, String> firstName;
	public static volatile SingularAttribute<TaskOverview, Long> dateOfReceipt;
	public static volatile SingularAttribute<TaskOverview, Long> id;
	public static volatile SingularAttribute<TaskOverview, Boolean> notificationCompleted;
	public static volatile SingularAttribute<TaskOverview, String> taskID;
	public static volatile SingularAttribute<TaskOverview, Boolean> stainingCompleted;

}

