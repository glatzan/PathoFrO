package com.patho.main.model.interfaces;

import com.patho.main.model.patient.Task;

public interface IdManuallyAltered {

	public boolean isIdManuallyAltered();

	public void setIdManuallyAltered(boolean idManuallyAltered);

	public void updateAllNames(boolean useAutoNomenclature, boolean ignoreManuallyNamedItems);

	public Task getTask();
}
