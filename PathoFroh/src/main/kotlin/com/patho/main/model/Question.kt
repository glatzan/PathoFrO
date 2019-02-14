package com.patho.main.model

import com.patho.main.common.Eye
import com.patho.main.common.TaskPriority
import com.patho.main.model.patient.Patient
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.envers.Audited
import java.time.Instant
import java.time.LocalDate
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
open class Question {

    @Id
    @GeneratedValue(generator = "question_generator")
    @SequenceGenerator(name = "question_generator", sequenceName = "question_sequence", initialValue = 1000)
    open var id: Long = 0

    @NotBlank
    @Size(min = 3, max = 100)
    open var title: String? = null

    @Column(columnDefinition = "text")
    open var description: String? = null

    @Column
    open var ddate = LocalDate.now()

    @Column
    open var dadate = LocalDate.now()

    @Column
    open var ddat2e = LocalDate.now()


    @Column
    open var idate = Instant.now()


    /**
     * Generated Task ID as String
     */
    @Column(length = 6)
    open var taskID = "233322"

    /**
     * The Patient of the task;
     */
    @ManyToOne(targetEntity = Patient::class)
    open var parent: Patient? = null

    /**
     * If true the program will provide default names for samples and blocks
     */
    @Column
    open var useAutoNomenclature: Boolean = false

    /**
     * Priority of the task
     */
    @Enumerated(EnumType.ORDINAL)
    open var taskPriority: TaskPriority = TaskPriority.NONE

    /**
     */
    @Column
    open var typeOfOperation: Byte = 0

    /**
     * Details of the case
     */
    @Column(columnDefinition = "VARCHAR")
    open var caseHistory = ""

    /**
     * Details of the case
     */
    @Column(columnDefinition = "VARCHAR")
    open var commentary = ""

    /**
     * Insurance of the patient
     */
    @Column(columnDefinition = "VARCHAR")
    open var insurance: String = ""

    /**
     * Ward of the patient
     */
    @Column
    open var ward = ""

    /**
     * Ey of the samples right/left/both
     */
    @Enumerated(EnumType.STRING)
    open var eye = Eye.UNKNOWN

    /**
     * date of staining completion
     */
    @Column
    open var stainingCompletionDate: Instant? = null

    /**
     * Date of diagnosis finalization
     */
    @Column
    open var diagnosisCompletionDate: Instant? = null

    /**
     * The date of the completion of the notificaiton.
     */
    @Column
    open var notificationCompletionDate: Instant? = null
}