package com.patho.main.model


import com.patho.main.common.TaskPriority
import com.patho.main.model.interfaces.FullName
import com.patho.main.model.person.Person
import lombok.Getter
import lombok.Setter
import org.hibernate.annotations.Immutable
import java.time.LocalDate
import javax.persistence.*

@NamedNativeQuery(
        name = "TaskOverview.findAllWithAssociatedPerson",
        query = "Select " +
                "task.id, " +
                "task.taskId, " +
                "task.receiptDate, " +
                "case when task.stainingcompletiondate is not null then true else false end as stainingcompleted, " +
                "case when task.diagnosiscompletiondate is not null then true else false end as diagnosiscompleted, " +
                "case when task.notificationcompletiondate is not null then true else false end as notificationcompleted, " +
                "task.finalized, " +
                "task.taskpriority, " +
                "pers.title, " +
                "pers.firstname, " +
                "pers.lastname, " +
                "pers.birthday, " +
                "case when EXISTS(select contact.id from reporttransmitter as contact where task.id = contact.task_id and contact.person_id = :personid) then true else false end as usercontact " +
                "from task as task " +
                "left join patient pat on pat.id = task.parent_id " +
                "left join person as pers on pers.id = pat.person_id " +
                "order by task.taskId desc ",
        resultSetMapping = "TaskOverview")
@SqlResultSetMapping(
        name = "TaskOverview",
        classes = [ConstructorResult(
                targetClass = TaskOverview::class,
                columns =
                [
                    ColumnResult(name = "id", type = Long::class),
                    ColumnResult(name = "taskid", type = String::class),
                    ColumnResult(name = "receiptDate", type = LocalDate::class),
                    ColumnResult(name = "stainingcompleted", type = Boolean::class),
                    ColumnResult(name = "diagnosiscompleted", type = Boolean::class),
                    ColumnResult(name = "notificationcompleted", type = Boolean::class),
                    ColumnResult(name = "finalized", type = Boolean::class),
                    ColumnResult(name = "title", type = String::class),
                    ColumnResult(name = "firstname", type = String::class),
                    ColumnResult(name = "lastname", type = String::class),
                    ColumnResult(name = "birthday", type = LocalDate::class),
                    ColumnResult(name = "taskpriority", type = Int::class),
                    ColumnResult(name = "usercontact", type = Boolean::class)
                ]
        )]
)
@Entity
@Immutable
@Getter
@Setter
class TaskOverview : FullName {

    @Id
    val id: Long

    val taskID: String

    val receiptDate: LocalDate

    val stainingCompleted: Boolean

    val diagnosisCompleted: Boolean

    val notificationCompleted: Boolean

    val finalized: Boolean

    override var title: String

    override var lastName: String

    override var firstName: String

    val birthday: LocalDate

    @Enumerated(EnumType.ORDINAL)
    val taskPriority: TaskPriority

    val userContact: Boolean

    constructor(id: Long, taskID: String, receiptDate: LocalDate, stainingCompleted: Boolean,
                diagnosisCompleted: Boolean, notificationCompleted: Boolean, finalized: Boolean, title: String, lastName: String,
                firstName: String, birthday: LocalDate, taskPriority: Int, userContact: Boolean) {
        this.id = id
        this.taskID = taskID
        this.receiptDate = receiptDate
        this.stainingCompleted = stainingCompleted
        this.diagnosisCompleted = diagnosisCompleted
        this.notificationCompleted = notificationCompleted
        this.finalized = finalized
        this.title = title
        this.lastName = lastName
        this.firstName = firstName
        this.birthday = birthday
        this.taskPriority = TaskPriority.values()[taskPriority]
        this.userContact = userContact
    }

    override var gender: Person.Gender
        get() = Person.Gender.UNKNOWN
        set(value) {}
}