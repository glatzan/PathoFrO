package com.patho.main.model;

import com.patho.main.common.CouncilState;
import com.patho.main.model.patient.Task;
import com.patho.main.model.util.audit.Audit;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Council.class)
public abstract class Council_ {

	public static volatile SingularAttribute<Council, Boolean> expectSampleReturn;
	public static volatile SingularAttribute<Council, Boolean> councilRequestCompleted;
	public static volatile SingularAttribute<Council, String> sampleShippedCommentary;
	public static volatile SingularAttribute<Council, Date> sampleReturnedDate;
	public static volatile SetAttribute<Council, PDFContainer> attachedPdfs;
	public static volatile SingularAttribute<Council, Long> version;
	public static volatile SingularAttribute<Council, String> councilText;
	public static volatile SingularAttribute<Council, Physician> physicianRequestingCouncil;
	public static volatile SingularAttribute<Council, Task> task;
	public static volatile SingularAttribute<Council, Boolean> sampleShipped;
	public static volatile SingularAttribute<Council, Date> replyReceivedDate;
	public static volatile SingularAttribute<Council, Audit> audit;
	public static volatile SingularAttribute<Council, String> name;
	public static volatile SingularAttribute<Council, CouncilState> councilState;
	public static volatile SingularAttribute<Council, Long> id;
	public static volatile SingularAttribute<Council, Boolean> sampleReturned;
	public static volatile SingularAttribute<Council, Date> dateOfRequest;
	public static volatile SingularAttribute<Council, Date> sampleShippedDate;
	public static volatile SingularAttribute<Council, String> sampleReturnedCommentary;
	public static volatile SingularAttribute<Council, Boolean> replyReceived;
	public static volatile SingularAttribute<Council, Physician> councilPhysician;
	public static volatile SingularAttribute<Council, String> commentary;

}

