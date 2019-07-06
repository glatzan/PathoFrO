package com.patho.main.model;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.envers.Audited;

import javax.persistence.*;



@Entity
@Audited
@SelectBeforeUpdate(true)
@DynamicUpdate(true)
@SequenceGenerator(name = "signature_sequencegenerator", sequenceName = "signature_sequence")
public class Signature{

	private long id;

	private long version;

	private Physician physician;

	private String role;

	public Signature() {
	}

	public Signature(Physician physician) {
		this.physician = physician;
		this.role = physician != null ? physician.getClinicRole() : "";
	}

	@Id
	@GeneratedValue(generator = "signature_sequencegenerator")
	@Column(unique = true, nullable = false)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Version
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@OneToOne
	public Physician getPhysician() {
		return physician;
	}

	public void setPhysician(Physician physician) {
		this.physician = physician;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Signature && ((Signature) obj).getPhysician() != null && getPhysician() != null) {
			if (((Signature) obj).getPhysician().getId() == getPhysician().getId())
				return true;
		}

		return super.equals(obj);
	}

	/**
	 * Updates the signature with a new physician, if the same physician nothing
	 * will be done.
	 * 
	 * @param physician
	 */
	public void updateSignature(Physician physician) {
		if (physician == null) {
			setPhysician(null);
			setRole("");
			return;
		}

		if (getPhysician() == null || getPhysician().getId() != physician.getId()) {
			setPhysician(physician);
			setRole(physician.getClinicRole());
		}
	}

}