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
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;

import com.patho.main.common.Dialog;
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
@SequenceGenerator(name = "block_sequencegenerator", sequenceName = "block_sequence")
@Getter
@Setter
public class Block
		implements LogAble, TaskEntity, Parent<Sample>, PatientRollbackAble<Sample>, IdManuallyAltered, ID {

	@Id
	@GeneratedValue(generator = "block_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Version
	private long version;

	/**
	 * Parent of this block
	 */
	@ManyToOne
	private Sample parent;

	/**
	 * ID in block
	 */
	@Column
	private String blockID = "";

	/**
	 * True if the user has manually altered the sample ID
	 */
	@Column
	private boolean idManuallyAltered;

	/**
	 * staining array
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "parent")
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("creationDate ASC, id ASC")
	private List<Slide> slides = new ArrayList<Slide>();

	/**
	 * Date of sample creation
	 */
	@Column
	private long creationDate;

	@Transient
	public void removeStaining(Slide staining) {
		getSlides().remove(staining);
	}

	@Transient
	public boolean updateNameOfBlock(boolean useAutoNomenclature, boolean ignoreManuallyNamedItems) {
		if (!isIdManuallyAltered() || (ignoreManuallyNamedItems && isIdManuallyAltered())) {
			if (useAutoNomenclature) {
				String name;

				if (parent.getBlocks().size() > 1) {
					name = TaskUtil.getCharNumber(getParent().getBlocks().indexOf(this));
					if (getTask().getSamples().size() == 1)
						name = name.toUpperCase();
				} else {
					// no block name
					name = "";
				}

				if (getBlockID() == null || !getBlockID().equals(name)) {
					setBlockID(name);
					setIdManuallyAltered(false);
					return true;
				}
			}
		}

		return false;
	}

	@Transient
	public void updateAllNames() {
		updateAllNames(getTask().isUseAutoNomenclature(), false);
	}

	@Transient
	public void updateAllNames(boolean useAutoNomenclature, boolean ignoreManuallyNamedItems) {
		updateNameOfBlock(useAutoNomenclature, ignoreManuallyNamedItems);
		getSlides().stream().forEach(p -> p.updateNameOfSlide(useAutoNomenclature, ignoreManuallyNamedItems));
	}

	@Override
	public String toString() {
		return "Block: " + getBlockID() + (getId() != 0 ? ", ID: " + getId() : "");
	}
	
	/**
	 * Overwriting of TaskEntity, returns id
	 */
	@Transient
	@Override
	public String toSimpleString() {
		return getBlockID();
	}

	/********************************************************
	 * Interface Parent
	 ********************************************************/
	/**
	 * �berschreibt Methode aus dem Interface StainingTreeParent
	 */
	@Transient
	public Patient getPatient() {
		return getParent().getPatient();
	}

	/**
	 * Returns the parent task
	 */
	@Override
	@Transient
	public Task getTask() {
		return getParent().getTask();
	}

	/********************************************************
	 * Interface Parent
	 ********************************************************/

	
}
