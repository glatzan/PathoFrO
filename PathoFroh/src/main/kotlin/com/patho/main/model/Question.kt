package com.patho.main.model

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
class Question {

    @Id
    @GeneratedValue(generator = "question_generator")
    @SequenceGenerator(name = "question_generator", sequenceName = "question_sequence", initialValue = 1000)
    var id: Long = 0

    @NotBlank
    @Size(min = 3, max = 100)
    var title: String? = null

    @Column(columnDefinition = "text")
    var description: String? = null

    @Column
    var ddate = LocalDate.now()

    @Column
    var idate = Instant.now()

}