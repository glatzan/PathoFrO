package com.patho.main.action.dialog.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.interfaces.TaskEntity;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.service.BlockService;
import com.patho.main.service.SampleService;
import com.patho.main.service.SlideService;
import com.patho.main.service.WorkPhaseService;
import com.patho.main.util.dialogReturn.StainingPhaseUpdateEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Configurable
@Getter
@Setter
public class DeleteTaskEntityDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SampleService sampleService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SlideService slideService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private BlockService blockService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private WorkPhaseService workPhaseService;

	/**
	 * Temporary save for a task tree entity (sample, slide, block)
	 */
	private TaskEntity toDelete;

	/**
	 * True if the staining phase has ended after deleting an entity
	 */
	private boolean StainingPhaseEnded;

	public void initAndPrepareBean(Task task, TaskEntity entity) {
		if (initBean(task, entity))
			prepareDialog();
	}

	public boolean initBean(Task task, TaskEntity entity) {
		super.initBean(task, Dialog.DELETE_TASK_ENTITY);

		setToDelete(entity);

		return true;
	}

	/**
	 * Deletes samples, slides and blocks
	 */
	public void deleteAndHide() {
		Task t = null;
		if (toDelete instanceof Slide) {
			t = slideService.deleteSlideAndPersist((Slide) toDelete);
		} else if (toDelete instanceof Block) {
			t = blockService.deleteBlockAndPersist((Block) toDelete);
		} else if (toDelete instanceof Sample) {
			t = sampleService.deleteSampleAndPersist((Sample) toDelete);
		}

		hideDialog(new StainingPhaseUpdateEvent(t));
	}
}
