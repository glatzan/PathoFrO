package com.patho.main.action.dialog.slides;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.TaskRepository;
import com.patho.main.service.TaskService;
import com.patho.main.util.dialog.event.TaskReloadEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
@Configurable
@Getter
@Setter
public class SlideNamingDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskRepository taskRepository;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private TaskService taskService;

	private boolean useAutoNomeclature;

	private boolean dataChanged;

	public SlideNamingDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task) {
		super.initBean(task, Dialog.SLIDE_NAMING);
		setUseAutoNomeclature(task.getUseAutoNomenclature());
		setDataChanged(false);
		return true;
	}

	public void saveAndHide() {
		task.setUseAutoNomenclature(isUseAutoNomeclature());
		hideDialog(new TaskReloadEvent(taskRepository.save(task, resourceBundle.get("log.patient.task.update"))));
		MessageHandler.sendGrowlMessagesAsResource("growl.task.saved", "growl.task.saved.text");
	}

	public void renameAndHide(boolean ignoreManuallyChangedEntities) {
		hideDialog(
				new TaskReloadEvent(taskService.updateNamesOfTaskEntities(task, ignoreManuallyChangedEntities, true)));
		MessageHandler.sendGrowlMessagesAsResource("growl.task.updateNaming", "growl.task.updateNaming.text");
	}
}
