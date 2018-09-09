package com.patho.main.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;

import com.patho.main.common.InformedConsentType;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Task;

import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "bioBank_sequencegenerator", sequenceName = "bioBank_sequence")
@Getter
@Setter
public class BioBank implements DataList {

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
}
