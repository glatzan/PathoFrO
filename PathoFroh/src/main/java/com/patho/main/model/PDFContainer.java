package com.patho.main.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import com.patho.main.model.interfaces.ID;
import com.patho.main.model.util.audit.Audit;
import com.patho.main.model.audit.AuditAble;
import com.patho.main.model.util.audit.AuditListener;
import com.patho.main.template.PrintDocument.DocumentType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@SelectBeforeUpdate(true)
@SequenceGenerator(name = "pdfs_sequencegenerator", sequenceName = "pdfs_sequence")
@Getter
@Setter
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
}
