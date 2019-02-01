package com.patho.main.model;

import java.io.File;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;

import com.patho.main.common.InformedConsentType;
import com.patho.main.config.PathoConfig;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Task;
import com.patho.main.model.util.audit.Audit;
import com.patho.main.model.util.audit.AuditAble;
import com.patho.main.model.util.audit.AuditListener;

import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@SelectBeforeUpdate
@SequenceGenerator(name = "bioBank_sequencegenerator", sequenceName = "bioBank_sequence")
@Getter
@Setter
@EntityListeners(AuditListener.class)
public class BioBank implements DataList, AuditAble {

	@Id
	@GeneratedValue(generator = "bioBank_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Version
	private long version;

	@OneToOne(fetch = FetchType.LAZY)
	private Task task;

	@Enumerated(EnumType.STRING)
	private InformedConsentType informedConsentType;

	@Embedded
	private Audit audit;

	/**
	 * Date of informed constent retraction
	 */
	@Temporal(TemporalType.DATE)
	private Date retractionDate;

	/**
	 * Date of informed constent retraction
	 */
	@Temporal(TemporalType.DATE)
	private Date consentDate;

	/**
	 * Text of council
	 */
	@Column(columnDefinition = "text")
	private String commentary;

	@OneToMany()
	@LazyCollection(LazyCollectionOption.TRUE)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("audit.createdOn DESC")
	private Set<PDFContainer> attachedPdfs;

	@Override
	@Transient
	public String getPublicName() {
		return task.getTaskID();
	}
	
	/**
	 * File Repository Base of the corresponding patient
	 */
	@Override
	@Transient
	public File getFileRepositoryBase() {
		return new File(PathoConfig.FileSettings.FILE_REPOSITORY_PATH_TOKEN + task.getParent().getId());
	}
}
