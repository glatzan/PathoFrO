package com.patho.main.model.scanner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.patho.main.model.AbstractPersistable
import com.patho.main.model.patient.Task
import org.hibernate.annotations.SelectBeforeUpdate
import java.io.Serializable
import javax.persistence.*

@Entity
@SelectBeforeUpdate(true)
class ScannedSlide : AbstractPersistable() {

    /**
     *
     */
    @Id
    @SequenceGenerator(name = "scannedslides_sequencegenerator", sequenceName = "scannedslides_sequence")
    @GeneratedValue(generator = "scannedslides_sequencegenerator")
    @Column(unique = true, nullable = false)
    override var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    var task: Task = Task()

    /**
     * Current Name
     */
    @Column(columnDefinition = "VARCHAR", length = 255)
    var name: String = ""

    /**
     * Global ID of the Slide
     */
    @Column(name = "slide_id")
    var globalSlideID: Long = 0

    /**
     * Unique Slide ID in Task
     */
    @Column
    var uniqueSlideID: Int = 0

    /**
     * Path of the slide
     */
    @Column(columnDefinition = "VARCHAR")
    var path: String = ""
}
