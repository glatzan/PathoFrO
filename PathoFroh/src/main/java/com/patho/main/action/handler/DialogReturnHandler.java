package com.patho.main.action.handler;

import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.patho.main.action.UserHandlerAction;
import com.patho.main.action.dialog.diagnosis.DiagnosisPhaseExitDialog.DiagnosisPhaseExitData;
import com.patho.main.action.dialog.miscellaneous.DeleteDialog.DeleteEvent;
import com.patho.main.action.dialog.patient.MergePatientDialog.PatientMergeEvent;
import com.patho.main.action.dialog.settings.UserSettingsDialog.ReloadUserEvent;
import com.patho.main.action.dialog.slides.StainingPhaseExitDialog.StainingPhaseExitData;
import com.patho.main.action.dialog.worklist.WorklistSearchDialog.WorklistSearchReturnEvent;
import com.patho.main.action.handler.GlobalEditViewHandler.TaskInitilize;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.service.BlockService;
import com.patho.main.service.SampleService;
import com.patho.main.service.SlideService;
import com.patho.main.util.dialogReturn.DiagnosisPhaseUpdateEvent;
import com.patho.main.util.dialogReturn.PatientReturnEvent;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;
import com.patho.main.util.dialogReturn.StainingPhaseUpdateEvent;

@Controller
@Scope("session")
public class DialogReturnHandler {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalEditViewHandler globalEditViewHandler;

	@Autowired
	private WorklistHandler worklistHandler;

	@Autowired
	private WorkPhaseHandler workPhaseHandler;

	@Autowired
	private WorklistViewHandler worklistViewHandler;

	@Autowired
	private UserHandlerAction userHandlerAction;

	@Autowired
	private SlideService slideService;

	@Autowired
	private BlockService blockService;

	@Autowired
	private SampleService sampleService;

	/**
	 * 
	 * Patient add dialog return function
	 * 
	 * @param event
	 */
	public void onSearchForPatientReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof PatientReturnEvent) {
			logger.debug("Patient was selected, adding to database and worklist");
			Patient p = ((PatientReturnEvent) event.getObject()).getPatien();
			// reload if patient is known to database, and may is associated with tasks
			worklistViewHandler.addPatientToWorkList(p, true,true);
		} else {
			logger.debug("No Patient was selected");
		}
	}

	/**
	 * Dialog return function, reloads the selected task
	 * 
	 * @param event
	 */
	public void onDefaultDialogReturn(SelectEvent event) {
		if (event.getObject() != null) {
			// Patient reload event
			if (event.getObject() instanceof PatientReturnEvent) {
				logger.debug("Patient add event reload event.");
				if (((PatientReturnEvent) event.getObject()).getTask() != null)
					worklistHandler.getCurrent().setSelectedTask(((PatientReturnEvent) event.getObject()).getTask());
				worklistHandler.getCurrent().reloadSelectedPatientAndTask();

				globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
				// staining phase reload event
			} else if (event.getObject() instanceof DiagnosisPhaseUpdateEvent) {
				logger.debug("Update Diagnosis phase");
				// reload task
				worklistHandler.getCurrent().reloadSelectedPatientAndTask();
				globalEditViewHandler.generateViewData(TaskInitilize.GENERATE_TASK_STATUS);
				workPhaseHandler.updateDiagnosisPhase(worklistHandler.getCurrent().getSelectedTask());
			} else if (event.getObject() instanceof ReloadTaskEvent || event.getObject() instanceof ReloadEvent) {
				logger.debug("Task reload event.");
				if (event.getObject() instanceof ReloadTaskEvent
						&& ((ReloadTaskEvent) event.getObject()).getTask() != null)
					worklistViewHandler.replaceTaskInWorklist(((ReloadTaskEvent) event.getObject()).getTask());
				else
					worklistViewHandler.reloadCurrentTask();
			}
		}
	}

	/**
	 * Is called on return of a worklist selection via dialog
	 * 
	 * @param event
	 */
	public void onWorklistSelectReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof WorklistSearchReturnEvent) {
			logger.debug("Setting new worklist");
			worklistViewHandler.addWorklist(((WorklistSearchReturnEvent) event.getObject()).getWorklist(), true);
			return;
		}
		onDefaultDialogReturn(event);
	}

	/**
	 * Is called on return of the patient data edit dialog, if a merge event had
	 * happened the worklist is updated.
	 * 
	 * @param event
	 */
	public void onPatientMergeReturn(SelectEvent event) {
		if (event.getObject() != null && event.getObject() instanceof PatientMergeEvent) {
			PatientMergeEvent p = (PatientMergeEvent) event.getObject();

			if (p.getSource().getArchived())
				worklistViewHandler.removePatientFromWorklist(p.getSource());
			else
				worklistViewHandler.replacePatientInWorklist(p.getSource());

			if (p.getTarget().getArchived())
				worklistViewHandler.removePatientFromWorklist(p.getTarget());
			else
				worklistViewHandler.replacePatientInWorklist(p.getTarget());

			return;
		}

		onDefaultDialogReturn(event);
	}

	public void onStatingPhaseExitReturn(SelectEvent event) {
		if (event.getObject() instanceof StainingPhaseExitData) {
			logger.debug("Staining phase exit dialog return");
			StainingPhaseExitData data = (StainingPhaseExitData) event.getObject();

			workPhaseHandler.endStainingPhase(data.getTask(), data.isEndStainingPhase(),
					data.isRemoveFromStainingList(), data.isGoToDiagnosisPhase(), data.isRemoveFromWorklist());

			worklistViewHandler.reloadCurrentTask();

			return;
		}
		onDefaultDialogReturn(event);
	}

	public void onDiagnosisPhaseExitReutrn(SelectEvent event) {
		if (event.getObject() instanceof DiagnosisPhaseExitData) {
			logger.debug("Diagnosis phase exit dialog return");

			DiagnosisPhaseExitData data = (DiagnosisPhaseExitData) event.getObject();
			workPhaseHandler.endDiagnosisPhase(data.getTask(),
					data.isAllRevisions() ? null : data.getSelectedRevision(), data.isRemoveFromDiangosisList(),
					data.isNotification(), data.isRemoveFromWorklist());

			worklistViewHandler.reloadCurrentTask();
			return;
		}
		onDefaultDialogReturn(event);
	}

	public void onUserSettingsReturn(SelectEvent event) {
		if (event.getObject() instanceof ReloadUserEvent) {
			logger.debug("Updating user");
			userHandlerAction.updateCurrentUser();
			return;
		}

		onDefaultDialogReturn(event);
	}

	public void onStainingChangeReturn(SelectEvent event) {
		if (event.getObject() instanceof StainingPhaseUpdateEvent) {
			logger.debug("Update Stating phase");
			workPhaseHandler.updateStainingPhase(((StainingPhaseUpdateEvent) event.getObject()).getTask());
			return;
		}

		onDefaultDialogReturn(event);
	}

	/**
	 * Called on task entity delete return
	 * 
	 * @param event
	 */
	public void onDeleteTaskEntityReturn(SelectEvent event) {
		if (event.getObject() instanceof DeleteEvent) {

			DeleteEvent deleteEvent = (DeleteEvent) event.getObject();

			logger.debug("Deleteing task entity object");

			Task t = null;
			if (deleteEvent.getObject() instanceof Slide) {
				t = slideService.deleteSlideAndPersist((Slide) deleteEvent.getObject());
			} else if (deleteEvent.getObject() instanceof Block) {
				t = blockService.deleteBlockAndPersist((Block) deleteEvent.getObject());
			} else if (deleteEvent.getObject() instanceof Sample) {
				t = sampleService.deleteSampleAndPersist((Sample) deleteEvent.getObject());
			}

			onStainingChangeReturn(
					new SelectEvent(event.getComponent(), event.getBehavior(), new StainingPhaseUpdateEvent(t)));
			return;
		}

		onDefaultDialogReturn(event);
	}
}
