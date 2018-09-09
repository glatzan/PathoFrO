package com.patho.main.model.patient;

import com.patho.main.common.Eye;
import com.patho.main.common.TaskPriority;
import com.patho.main.model.Accounting;
import com.patho.main.model.AssociatedContact;
import com.patho.main.model.Council;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.favourites.FavouriteList;
import com.patho.main.model.util.audit.Audit;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Task.class)
public abstract class Task_ {

	public static volatile SingularAttribute<Task, String> insurance;
	public static volatile SingularAttribute<Task, Patient> parent;
	public static volatile SingularAttribute<Task, Integer> slideCounter;
	public static volatile SingularAttribute<Task, Long> dateOfSugery;
	public static volatile SingularAttribute<Task, Long> dueDate;
	public static volatile SetAttribute<Task, PDFContainer> attachedPdfs;
	public static volatile SingularAttribute<Task, String> ward;
	public static volatile SingularAttribute<Task, Accounting> accounting;
	public static volatile SingularAttribute<Task, Boolean> useAutoNomenclature;
	public static volatile SingularAttribute<Task, Audit> audit;
	public static volatile SingularAttribute<Task, String> caseHistory;
	public static volatile SingularAttribute<Task, Long> id;
	public static volatile SingularAttribute<Task, Long> notificationCompletionDate;
	public static volatile SingularAttribute<Task, String> taskID;
	public static volatile SingularAttribute<Task, TaskPriority> taskPriority;
	public static volatile SingularAttribute<Task, Long> version;
	public static volatile ListAttribute<Task, FavouriteList> favouriteLists;
	public static volatile ListAttribute<Task, Sample> samples;
	public static volatile SingularAttribute<Task, Eye> eye;
	public static volatile SingularAttribute<Task, Boolean> finalized;
	public static volatile SingularAttribute<Task, Long> diagnosisCompletionDate;
	public static volatile SingularAttribute<Task, Long> finalizationDate;
	public static volatile SingularAttribute<Task, Long> dateOfReceipt;
	public static volatile SingularAttribute<Task, Long> stainingCompletionDate;
	public static volatile SetAttribute<Task, DiagnosisRevision> diagnosisRevisions;
	public static volatile SetAttribute<Task, Council> councils;
	public static volatile SingularAttribute<Task, Byte> typeOfOperation;
	public static volatile SingularAttribute<Task, String> commentary;
	public static volatile ListAttribute<Task, AssociatedContact> contacts;

}

