package com.patho.main.model.patient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.patho.main.model.StainingPrototype;
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
@SequenceGenerator(name = "slide_sequencegenerator", sequenceName = "slide_sequence")
@Getter
@Setter
public class Slide implements Parent<Block>, LogAble, TaskEntity, PatientRollbackAble<Block>, IdManuallyAltered, ID {

	@Id
	@GeneratedValue(generator = "slide_sequencegenerator")
	@Column(unique = true, nullable = false)
	private long id;

	@Version
	private long version;

	@Column
	private int uniqueIDinTask;

	@Column
	private String slideID = "";

	@Column
	private boolean idManuallyAltered;

	@Column
	private long creationDate;

	@Column
	private long completionDate;
	
	@Column
	private boolean stainingCompleted;

	@Column
	private boolean reStaining;
	
	@Column(columnDefinition = "VARCHAR")
	private String commentary = "";

	@OneToOne
	@NotAudited
	private StainingPrototype slidePrototype;

	@ManyToOne
	private Block parent;

	@Transient
	public void updateAllNames() {
		updateNameOfSlide(getTask().isUseAutoNomenclature(), false);
	}
	
	@Transient
	public void updateAllNames(boolean useAutoNomenclature, boolean ignoreManuallyNamedItems) {
		updateNameOfSlide(useAutoNomenclature, ignoreManuallyNamedItems);
	}

	@Transient
	public boolean updateNameOfSlide(boolean useAutoNomenclature, boolean ignoreManuallyNamedItems) {
		if (!isIdManuallyAltered() || (isIdManuallyAltered() && ignoreManuallyNamedItems)) {
			StringBuilder name = new StringBuilder();

			if (useAutoNomenclature) {

				// generating block id
				name.append(parent.getParent().getSampleID());
				name.append(parent.getBlockID());
				if (name.length() > 0)
					name.append(" ");

				name.append(slidePrototype.getName());

				int stainingsInBlock = TaskUtil.getNumerOfSameStainings(this);

				if (stainingsInBlock > 1)
					name.append(String.valueOf(stainingsInBlock));

				if (getSlideID() == null || !getSlideID().equals(name.toString())) {
					setSlideID(name.toString());
					setIdManuallyAltered(false);
					return true;
				}
			} else if (getSlideID() == null || getSlideID().isEmpty()) {
				// only setting the staining and the number of the stating
				name.append(slidePrototype.getName());

				int stainingsInBlock = TaskUtil.getNumerOfSameStainings(this);

				if (stainingsInBlock > 1)
					name.append(String.valueOf(stainingsInBlock));

				setSlideID(name.toString());
				setIdManuallyAltered(false);
				return true;
			}
		}

		return false;
	}

	@Override
	@Transient
	public String toString() {
		return "Slide: " + getSlideID() + (getId() != 0 ? ", ID: " + getId() : "");
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
		return getParent().getTask();
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
		return getSlideID();
	}
}
