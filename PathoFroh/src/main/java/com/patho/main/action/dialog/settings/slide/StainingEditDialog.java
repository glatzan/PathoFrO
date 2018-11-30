package com.patho.main.action.dialog.settings.slide;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototypeDetails;
import com.patho.main.repository.StainingPrototypeRepository;
import com.patho.main.service.StainingPrototypeService;
import com.patho.main.util.dialogReturn.ReloadEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class StainingEditDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private StainingPrototypeRepository stainingPrototypeRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private StainingPrototypeService stainingPrototypeService;

	private boolean newStaining;

	private StainingPrototype stainingPrototype;

	private List<StainingPrototypeDetails> removeDetails;

	public StainingEditDialog initAndPrepareBean() {
		return initAndPrepareBean(new StainingPrototype());
	}

	public StainingEditDialog initAndPrepareBean(StainingPrototype staining) {
		if (initBean(staining))
			prepareDialog();
		return this;
	}

	public boolean initBean(StainingPrototype staining) {

		// reloading stating with batch details
		if (staining.getId() != 0) {
			setStainingPrototype(
					stainingPrototypeRepository.findOptionalByIdAndInitilize(staining.getId(), true).get());
			setNewStaining(false);
		} else {
			setNewStaining(true);
			setStainingPrototype(staining);
		}

		setRemoveDetails(new ArrayList<StainingPrototypeDetails>());

		return super.initBean(task, Dialog.SETTINGS_STAINING_EDIT);
	}

	public void addBatch() {
		StainingPrototypeDetails newBatch = new StainingPrototypeDetails(stainingPrototype);
		newBatch.setDeliveryDate(new Date(System.currentTimeMillis()));
		getStainingPrototype().getBatchDetails().add(0, newBatch);
	}

	public void removeBatch(StainingPrototypeDetails stainingPrototypeDetails) {
		stainingPrototype.getBatchDetails().remove(stainingPrototypeDetails);

		// delete if user clicks save
		if (stainingPrototypeDetails.getId() != 0) {
			getRemoveDetails().add(stainingPrototypeDetails);
		}
	}

	public void cloneBatch(StainingPrototypeDetails stainingPrototypeDetails) {
		try {
			StainingPrototypeDetails clone = stainingPrototypeDetails.clone();
			clone.setId(0);
			stainingPrototype.getBatchDetails().add(0, clone);
		} catch (CloneNotSupportedException e) {
		}
	}

	public void saveAndHide() {
		save();
		hideDialog(new ReloadEvent());
	}

	private void save() {
		stainingPrototypeService.addOrUpdate(getStainingPrototype(), getRemoveDetails());
	}
}
