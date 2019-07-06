package com.patho.main.model

import com.patho.main.model.audit.AuditAble
import com.patho.main.model.util.audit.Audit
import com.patho.main.model.util.audit.AuditListener
import com.patho.main.template.PrintDocumentType
import org.hibernate.annotations.SelectBeforeUpdate
import org.hibernate.annotations.Type
import org.hibernate.envers.Audited
import javax.persistence.*

@Entity
@Audited
@SelectBeforeUpdate(true)
@EntityListeners(AuditListener::class)
open class PDFContainer : AbstractPersistable, AuditAble {

    companion object {
        @JvmField
        val MARKER_DIAGNOSIS = "d:\$id"
    }

    @Id
    @SequenceGenerator(name = "pdfs_sequencegenerator", sequenceName = "pdfs_sequence")
    @GeneratedValue(generator = "pdfs_sequencegenerator")
    @Column(unique = true, nullable = false)
    override open var id: Long = 0

    // TODO remove
    @Type(type = "org.hibernate.type.BinaryType")
    open var data: ByteArray = ByteArray(0)

    @Enumerated(EnumType.STRING)
    open var type: PrintDocumentType = PrintDocumentType.EMPTY

    @Column(length = 255)
    open var name: String = ""

    @Embedded
    override var audit: Audit? = null

    @Column
    open var finalDocument: Boolean = false

    @Column(columnDefinition = "text")
    open var commentary: String = ""

    @Column(length = 10)
    open var intern: String = ""

    @Column(length = 255)
    open var path: String = ""

    @Column(length = 255)
    open var thumbnail: String = ""

    @Column
    open var restricted: Boolean = false

    open val isThumbnailPreset: Boolean
        @Transient
        get() = thumbnail != ""

    open val isPDFPreset: Boolean
        @Transient
        get() = path != ""

    constructor()

    constructor(type: PrintDocumentType, name: String = "", data: ByteArray = ByteArray(0)) {
        this.type = type
        this.data = data
        this.name = name
    }

    constructor(type: PrintDocumentType, name: String = "", path: String = "", thumbnail: String = "") {
        this.type = type
        this.path = path
        this.name = name
        this.thumbnail = thumbnail
    }
}