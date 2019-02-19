package com.patho.main.model.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import org.hibernate.annotations.Immutable;

import com.patho.main.common.TaskPriority;
import com.patho.main.model.interfaces.FullName;

import lombok.Getter;
import lombok.Setter;

@NamedNativeQuery(
	    name = "TaskOverview.findAllWithAssociatedPerson",
	    query =
	    "Select "+
	    	"task.id, "+ 
	    	"task.taskId, "+ 
	    	"task.receiptDate, "+
	    	"case when task.stainingcompletiondate is not null then true else false end as stainingcompleted, "+
			"case when task.diagnosiscompletiondate is not null then true else false end as diagnosiscompleted, "+
			"case when task.notificationcompletiondate is not null then true else false end as notificationcompleted, "+
			"task.finalized, "+ 
			"task.taskpriority, "+
			"pers.title, "+ 
			"pers.firstname, "+ 
			"pers.lastname, "+
			"pers.birthday, "+ 
			"case when EXISTS(select contact.id from reporttransmitter as contact where task.id = contact.task_id and contact.person_id = :personid) then true else false end as usercontact "+
			"from task as task "+ 
				"left join patient pat on pat.id = task.parent_id "+ 
				"left join person as pers on pers.id = pat.person_id "+
			"order by task.taskId desc ",
	    resultSetMapping = "TaskOverview"
	)
	@SqlResultSetMapping(
	    name = "TaskOverview",
	    classes = @ConstructorResult(
	        targetClass = TaskOverview.class,
	        columns = {
	            @ColumnResult(name = "id", type=Long.class),
	            @ColumnResult(name = "taskid", type=String.class ),
	            @ColumnResult(name = "receiptDate", type=LocalDate.class),
	            @ColumnResult(name = "stainingcompleted", type=Boolean.class),
	            @ColumnResult(name = "diagnosiscompleted", type=Boolean.class),
	            @ColumnResult(name = "notificationcompleted", type=Boolean.class),
	            @ColumnResult(name = "finalized", type=Boolean.class),
	            @ColumnResult(name = "title", type=String.class ),
	            @ColumnResult(name = "firstname", type=String.class ),
	            @ColumnResult(name = "lastname", type=String.class ),
	            @ColumnResult(name = "birthday", type=String.class),
	            @ColumnResult(name = "taskpriority", type=Integer.class ),
	            @ColumnResult(name = "usercontact", type=Boolean.class )
	        }
	    )
	)
@Entity
@Immutable
@Getter
@Setter
public class TaskOverview implements FullName {

	@Id
	private long id;
	private String taskID;
	private LocalDate receiptDate;
	private boolean stainingCompleted;
	private boolean diagnosisCompleted;
	private boolean notificationCompleted;
	private boolean finalized;
	private String title;
	private String lastName;
	private String firstName;
	private Date birthday;
	@Enumerated(EnumType.ORDINAL)
	private TaskPriority taskPriority;
	private boolean userContact;
	
	public TaskOverview(long id, String taskID, LocalDate receiptDate, boolean stainingCompleted,
						boolean diagnosisCompleted, boolean notificationCompleted, boolean finalized, String title, String lastName,
						String firstName, String birthday, int taskPriority, boolean userContact) {
		super();
		this.id = id;
		this.taskID = taskID;
		this.receiptDate = receiptDate;
		this.stainingCompleted = stainingCompleted;
		this.diagnosisCompleted = diagnosisCompleted;
		this.notificationCompleted = notificationCompleted;
		this.finalized = finalized;
		this.title = title;
		this.lastName = lastName;
		this.firstName = firstName;
		try {
			this.birthday = new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
		} catch (Exception e) {
		}
		this.taskPriority = TaskPriority.values()[taskPriority];
		this.userContact = userContact;
	}
	
	
}
