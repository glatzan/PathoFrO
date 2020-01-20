package com.patho.main.action.dialog.biobank;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.InformedConsentType;
import com.patho.main.model.patient.Task;
import com.patho.main.model.patient.miscellaneous.BioBank;
import com.patho.main.repository.BioBankRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.util.dialog.event.ReloadEvent;
import com.patho.main.util.dialog.event.TaskReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Date;
import java.util.Optional;
@Getter
@Setter
public class BioBankDialog extends AbstractDialog {

	private BioBank bioBank;

	/**
	 * Initializes the bean and shows the biobank dialog
	 * 
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
		super.hideDialog(new TaskReloadEvent());
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
		bioBank = SpringContextBridge.services().getBioBankRepository().save(bioBank, resourceBundle.get("log.patient.bioBank.save", getTask()),
				getTask().getPatient());
	}

	public void update(boolean reloadTask) {
		if (reloadTask) {
			Task oTask = SpringContextBridge.services().getTaskRepository().findByID(getTask().getId(), false, false, false,
					false, true);
			setTask(oTask);
		}

		// setting associatedBioBank
		Optional<BioBank> oBioBank = SpringContextBridge.services().getBioBankRepository().findOptionalByTaskAndInitialize(getTask());

		if (oBioBank.isPresent())
			setBioBank(oBioBank.get());
	}

}
