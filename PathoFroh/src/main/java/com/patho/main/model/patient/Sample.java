package com.patho.main.model.patient;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.patho.main.model.MaterialPreset;
import com.patho.main.model.interfaces.ID;
import com.patho.main.model.interfaces.IdManuallyAltered;
import com.patho.main.model.interfaces.LogAble;
import com.patho.main.model.interfaces.Parent;
import com.patho.main.model.interfaces.PatientRollbackAble;
import com.patho.main.model.interfaces.TaskEntity;
import com.patho.main.util.helper.TaskUtil;

import lombok.Getter;
import lombok.Setter;

@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "sample_sequencegenerator", sequenceName = "sample_sequence")
@Getter
@Setter
public class Sample implements Parent<Task>, LogAble, TaskEntity, PatientRollbackAble<Task>, IdManuallyAltered, ID {

	@Id
	@GeneratedValue(generator = "sample_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Version
	private long version;

	/**
	 * Parent of this sample.
	 */
	@ManyToOne(targetEntity = Task.class)
	private Task parent;

	/**
	 * Sample ID as string
	 */
	@Column
	private String sampleID = "";

	/**
	 * True if the user has manually altered the sample ID
	 */
	@Column
	private boolean idManuallyAltered;

	/**
	 * Date of sample creation
	 */
	@Column
	private long creationDate = 0;

	/**
	 * If true the not completed stainings are restainings.
	 */
	@Column
	private boolean reStainingPhase = false;

	/**
	 * blocks array
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("blockID ASC")
	private List<Block> blocks = new ArrayList<Block>();

	/**
	 * Material name is first initialized with the name of the typeOfMaterial. Can
	 * be later changed.
	 */
	private String material = "";

	/**
	 * Material object, containing preset for staining
	 */
	@OneToOne
	@NotAudited
	private MaterialPreset materialPreset;

	public Sample() {
	}

	/**
	 * Generates a sample name, if useAutoNomenclature is true an name will be auto
	 * generated
	 * 
	 * @param useAutoNomenclature
	 */
	@Transient
	public boolean updateNameOfSample(boolean useAutoNomenclature, boolean ignoreManuallyNamedItems) {
		if (!isIdManuallyAltered() || (ignoreManuallyNamedItems && isIdManuallyAltered())) {

			String name = null;

			if (useAutoNomenclature && getParent().getSamples().size() > 1)
				name = TaskUtil.getRomanNumber(getParent().getSamples().indexOf(this) + 1);
			else
				name = "";

			if (getSampleID() == null || !getSampleID().equals(name)) {
				setSampleID(name);
				setIdManuallyAltered(false);
				return true;
			}
		}

		return false;
	}

	@Transient
	public void updateAllNames() {
		updateAllNames(getTask().isUseAutoNomenclature(), false);
	}

	/**
	 * Updates the name of all block children
	 * 
	 * @param useAutoNomenclature
	 */
	@Transient
	public void updateAllNames(boolean useAutoNomenclature, boolean ignoreManuallyNamedItems) {
		updateNameOfSample(useAutoNomenclature, ignoreManuallyNamedItems);
		getBlocks().stream().forEach(p -> p.updateAllNames(useAutoNomenclature, ignoreManuallyNamedItems));
	}

	@Override
	@Transient
	public String toString() {
		return "Sample: " + getSampleID() + (getId() != 0 ? ", ID: " + getId() : "");
	}

	/********************************************************
	 * Interface Parent
	 ********************************************************/
	/**
	 * �berschreibt Methode aus dem Interface StainingTreeParent
	 */
	@Transient
	@Override
	public Patient getPatient() {
		return getParent().getPatient();
	}

	/**
	 * Returns the parent task
	 */
	@Override
	@Transient
	public Task getTask() {
		return getParent();
	}

	/********************************************************
	 * Interface Parent
	 ********************************************************/

	/**
	 * Overwriting of TaskEntity, returns id
	 */
	@Transient
	@Override
	public String toSimpleString() {
		return getSampleID();
	}
}
