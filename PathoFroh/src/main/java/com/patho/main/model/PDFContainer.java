package com.patho.main.model;

import com.patho.main.model.audit.AuditAble;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.util.audit.Audit;
import com.patho.main.model.util.audit.AuditListener;
import com.patho.main.template.PrintDocument.DocumentType;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Audited
@SelectBeforeUpdate(true)
@SequenceGenerator(name = "pdfs_sequencegenerator", sequenceName = "pdfs_sequence")
@EntityListeners(AuditListener.class)
public class PDFContainer implements ID, AuditAble {

	/**
	 * Internal marker for a diagnosis
	 */
	public final static String MARKER_DIAGNOSIS = "d:$id";

	@Id
	@GeneratedValue(generator = "pdfs_sequencegenerator")
	@Column(unique = true, nullable = false)
	protected long id;

	@Type(type = "org.hibernate.type.BinaryType")
	protected byte data[];

	@Enumerated(EnumType.STRING)
	protected DocumentType type;

	@Column(length = 255)
	protected String name;

	@Embedded
	protected Audit audit;

	@Column
	protected boolean finalDocument;

	@Column(columnDefinition = "text")
	protected String commentary;

	@Column(length = 10)
	protected String intern;

	@Column(length = 255)
	protected String path;

	@Column(length = 255)
	protected String thumbnail;

	@Column
	protected boolean restricted;

	public PDFContainer() {
	}

	public PDFContainer(DocumentType type) {
		this(type, "");
	}

	public PDFContainer(DocumentType type, byte[] data) {
		this(type, null, data);
	}

	public PDFContainer(DocumentType type, String name, byte[] data) {
		this.type = type;
		this.data = data;
		this.name = name;
	}

	public PDFContainer(DocumentType type, String path) {
		this(type, "", path, null);
	}

	public PDFContainer(DocumentType type, String name, String path) {
		this(type, name, path, null);
	}

	public PDFContainer(DocumentType type, String name, String path, String thumbnail) {
		this.type = type;
		this.path = path;
		this.name = name;
		this.thumbnail = thumbnail;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PDFContainer && ((PDFContainer) obj).getId() == getId())
			return true;

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getName() == null ? 0 : getName().hashCode() + (int) getId();
	}

	public long getId() {
		return this.id;
	}

	public byte[] getData() {
		return this.data;
	}

	public DocumentType getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public Audit getAudit() {
		return this.audit;
	}

	public boolean isFinalDocument() {
		return this.finalDocument;
	}

	public String getCommentary() {
		return this.commentary;
	}

	public String getIntern() {
		return this.intern;
	}

	public String getPath() {
		return this.path;
	}

	public String getThumbnail() {
		return this.thumbnail;
	}

	public boolean isRestricted() {
		return this.restricted;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setType(DocumentType type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAudit(Audit audit) {
		this.audit = audit;
	}

	public void setFinalDocument(boolean finalDocument) {
		this.finalDocument = finalDocument;
	}

	public void setCommentary(String commentary) {
		this.commentary = commentary;
	}

	public void setIntern(String intern) {
		this.intern = intern;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}
}
