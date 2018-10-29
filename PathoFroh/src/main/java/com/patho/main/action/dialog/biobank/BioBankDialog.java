package com.patho.main.action.dialog.biobank;

import java.util.Date;
import java.util.Optional;

import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.InformedConsentType;
import com.patho.main.model.BioBank;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.BioBankRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class BioBankDialog extends AbstractDialog<BioBankDialog> {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private BioBankRepository bioBankRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	private BioBank bioBank;

	/**
	 * Initializes the bean and shows the biobank dialog
	 * 
	 * @param patient
	 */
	public BioBankDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
		return this;
	}

	/**
	 * Initializes all field of the biobank object
	 * 
	 * @param task
	 */

	public boolean initBean(Task task) {
		setTask(task);
		update(false);
		return super.initBean(task, Dialog.BIO_BANK);
	}

	public void saveAndHide() {
		saveData();
		super.hideDialog(new ReloadTaskEvent());
	}

	/**
	 * Is called on type change
	 */
	public void onTypeChange() {
		if (bioBank.getInformedConsentType() == InformedConsentType.REVOKED)
			bioBank.setRetractionDate(new Date());
		else if (bioBank.getInformedConsentType() == InformedConsentType.FULL)
			bioBank.setConsentDate(new Date());

		saveData();
	}

	/**
	 * On dialog return, reload data
	 * 
	 * @param event
	 */
	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof ReloadEvent) {
			update(true);
		}
	}

	public void saveData() {
		bioBank = bioBankRepository.save(bioBank, resourceBundle.get("log.patient.bioBank.save", getTask()),
				getTask().getPatient());
	}

	private void update(boolean reloadTask) {
		if (reloadTask) {
			Optional<Task> oTask = taskRepository.findOptionalByIdAndInitialize(getTask().getId(), false, false, false,
					false, true);
			setTask(oTask.get());
		}

		// setting associatedBioBank
		Optional<BioBank> oBioBank = bioBankRepository.findOptionalByTaskAndInitialize(getTask());

		if (oBioBank.isPresent())
			setBioBank(oBioBank.get());
	}

}
