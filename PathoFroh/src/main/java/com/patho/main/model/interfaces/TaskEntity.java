package com.patho.main.model.interfaces;

public interface TaskEntity {
	/**
	 * Returns the class of the object
	 * 
	 * @return
	 */
	public default String getClassIdentifier() {
		return getClass().getSimpleName();
	}

	/**
	 * Returns a name or id
	 * 
	 * @return
	 */
	public String toSimpleString();
}
