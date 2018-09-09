package com.patho.main.action.dialog;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.patho.main.action.DialogHandlerAction;
import com.patho.main.action.dialog.AbstractTabDialog.AbstractTab;
import com.patho.main.action.handler.WorklistViewHandlerAction;
import com.patho.main.common.Dialog;
import com.patho.main.template.PrintDocument.DocumentType;
import com.patho.main.model.BioBank;
import com.patho.main.model.PDFContainer;
import com.patho.main.model.interfaces.DataList;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.BioBankRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Getter
@Setter
@Scope(value = "session")
public class BioBankDialogHandler extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private BioBankRepository bioBankRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorklistViewHandlerAction worklistViewHandlerAction;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DialogHandlerAction dialogHandlerAction;

	private BioBank bioBank;

	/**
	 * Initializes the bean and shows the biobank dialog
	 * 
	 * @param patient
	 */
	public void initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();
	}

	/**
	 * Initializes all field of the biobank object
	 * 
	 * @param task
	 */

	public boolean initBean(Task task) {
		Optional<Task> oTask = taskRepository.findOptionalByIdAndInitialize(task.getId(), false, false, true, false,
				false);

		if (oTask.isPresent()) {
			task = oTask.get();
			super.initBean(task, Dialog.BIO_BANK);
			// setting associatedBioBank
			Optional<BioBank> oBioBank = bioBankRepository.findOptionalByTask(task);
			if (!oBioBank.isPresent())
				return false;

			setBioBank(oBioBank.get());
			return true;
		}

		return false;
	}

	/**
	 * Saves the biobank to the database
	 */
	public void saveBioBank() {
		try {
			bioBankRepository.save(bioBank, resourceBundle.get("log.patient.bioBank.save", getTask()));
		} catch (HistoDatabaseInconsistentVersionException e) {
			onDatabaseVersionConflict();
		}
	}

	public void showMediaSelectDialog() {
		try {

			// init dialog for patient and task
			dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(),
					new DataList[] { getTask(), getTask().getPatient() }, true);

			// setting advance copy mode with move as true and target to task
			// and biobank
			dialogHandlerAction.getMediaDialog().enableAutoCopyMode(new DataList[] { getTask(), getBioBank() }, true,
					true);

			// enabeling upload to task
			dialogHandlerAction.getMediaDialog().enableUpload(new DataList[] { getTask() },
					new DocumentType[] { DocumentType.BIOBANK_INFORMED_CONSENT });

			// setting info text
			dialogHandlerAction.getMediaDialog().setActionDescription(
					resourceBundle.get("dialog.pdfOrganizer.headline.info.biobank", getTask().getTaskID()));

			// show dialog
			dialogHandlerAction.getMediaDialog().prepareDialog();
		} catch (HistoDatabaseInconsistentVersionException e) {
			// do nothing
			// TODO: infom user
		}
	}

	public void showMediaViewDialog(PDFContainer pdfContainer) {
		// init dialog for patient and task
		dialogHandlerAction.getMediaDialog().initBean(getTask().getPatient(), getBioBank(), pdfContainer, false);

		// setting info text
		dialogHandlerAction.getMediaDialog()
				.setActionDescription(resourceBundle.get("dialog.pdfOrganizer.headline.info.biobank", getTask().getTaskID()));

		// show dialog
		dialogHandlerAction.getMediaDialog().prepareDialog();
	}

}
