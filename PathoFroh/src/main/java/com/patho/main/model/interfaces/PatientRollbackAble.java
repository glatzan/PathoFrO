package com.patho.main.model.interfaces;


public interface PatientRollbackAble<T extends PatientRollbackAble<?>> extends ID, Parent<T> {

	/**
	 * Returns a hierarchical path for logging the object
	 * 
	 * @return
	 */
	public default String getLogPath() {
		return getParent().getLogPath() + " / " + toString();
	}
}
