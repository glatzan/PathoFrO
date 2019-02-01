package com.patho.main.model.interfaces;

import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;

/**
 * Interface for every object of the task tree (Task->Sample->Block->Staining).
 * Enables the returning of the parent and implements the archivable interface.
 * 
 * @author andi
 *
 * @param <T>
 */
public interface Parent<T extends Parent<?>> {

	public default Patient getPatient() {
		return getParent().getPatient();
	}

	public Task getTask();

	public T getParent();

	public void setParent(T parent);
}
