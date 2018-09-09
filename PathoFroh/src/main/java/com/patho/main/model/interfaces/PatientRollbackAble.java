package com.patho.main.model.interfaces;

import com.patho.main.model.patient.Patient;

public interface PatientRollbackAble<T extends PatientRollbackAble<?>> extends ID {

	public Patient getPatient();
	
	public T getParent();

	public void setParent(T parent);

	/**
	 * Returns a hierarchical path for logging the object
	 * 
	 * @return
	 */
	public default String getLogPath() {
		return getParent().getLogPath() + " / " + toString();
	}
}
