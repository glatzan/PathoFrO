package com.patho.main.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;

import com.patho.main.common.CouncilState;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.patient.Task;
import com.patho.main.util.helper.TextToLatexConverter;

import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "council_sequencegenerator", sequenceName = "council_sequence")
@Getter
@Setter
public class Council implements ID, DataList {

	@Id
	@GeneratedValue(generator = "council_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Version
	private long version;

	@ManyToOne(fetch = FetchType.LAZY)
	private Task task;

	/**
	 * Name of the council
	 */
	@Column(columnDefinition = "VARCHAR")
	private String name;

	/**
	 * Council physician
	 */
	@OneToOne
	private Physician councilPhysician;

	/**
	 * Physician to sign the council
	 */
	@OneToOne
	private Physician physicianRequestingCouncil;

	/**
	 * Text of council
	 */
	@Column(columnDefinition = "text")
	private String councilText;

	/**
	 * True if samples were send to external clinics
	 */
	@Column
	private boolean sampleShipped;
	/**
	 * Attached slides of the council
	 */
	@Column(columnDefinition = "text")
	private String sampleShippedCommentary;
	
	/**
	 * Date of request
	 */
	@Temporal(TemporalType.DATE)
	private Date sampleShippedDate;
	
	/**
	 * True if sample is returned
	 */
	@Column
	private boolean sampleReturned;
	
	/**
	 * Commentary
	 */
	@Column(columnDefinition = "text")
	private String sampleReturnedCommentary;

	/**
	 * Date of request
	 */
	@Temporal(TemporalType.DATE)
	private Date sampleReturnedDate;
	
	/**
	 * Date of request
	 */
	@Temporal(TemporalType.DATE)
	private Date dateOfRequest;

	/**
	 * State of the council
	 */
	@Enumerated(EnumType.ORDINAL)
	private CouncilState councilState;

	/**
	 * Pdf attached to this council
	 */
	@OneToMany(fetch = FetchType.LAZY)
	@OrderBy("audit.createdOn DESC")
	private Set<PDFContainer> attachedPdfs;

	public Council() {
	}

	public Council(Task task) {
		this.task = task;
	}

	@Transient
	public String getCouncilTextAsLatex() {
		return (new TextToLatexConverter()).convertToTex(getCouncilText());
	}

	@Transient
	public boolean isCouncilState(CouncilState... councilStates) {
		for (CouncilState councilState : councilStates) {
			if (getCouncilState() == councilState)
				return true;
		}

		return false;
	}

	@Override
	@Transient
	public String getPublicName() {
		return "Konsil - " + task.getTaskID();
	}
}
