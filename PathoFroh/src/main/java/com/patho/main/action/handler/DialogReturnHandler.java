package com.patho.main.action.handler;

import com.patho.main.action.dialog.diagnosis.DiagnosisPhaseExitDialog.DiagnosisPhaseExitData;
import com.patho.main.action.dialog.miscellaneous.DeleteDialog.DeleteEvent;
import com.patho.main.action.dialog.patient.MergePatientDialog.PatientMergeEvent;
import com.patho.main.action.dialog.settings.UserSettingsDialog.ReloadUserEvent;
import com.patho.main.action.dialog.slides.StainingPhaseExitDialog.StainingPhaseExitData;
import com.patho.main.action.dialog.worklist.WorklistSearchDialog.WorklistSearchReturnEvent;
import com.patho.main.model.patient.*;
import com.patho.main.service.BlockService;
import com.patho.main.service.SampleService;
import com.patho.main.service.SlideService;
import com.patho.main.service.UserService;
import com.patho.main.util.dialogReturn.PatientReturnEvent;
import com.patho.main.util.dialogReturn.ReloadEvent;
import com.patho.main.util.dialogReturn.ReloadTaskEvent;
import com.patho.main.util.event.dialog.StainingPhaseUpdateEvent;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("session")
public class DialogReturnHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorklistHandler worklistHandler;

    @Autowired
    private WorkPhaseHandler workPhaseHandler;

    @Autowired
    private UserService userService;

    @Autowired
    private SlideService slideService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private CentralHandler centralHandler;

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

                centralHandler.loadViews(CentralHandler.Load.RELOAD_TASK_STATUS);
                // staining phase reload event
            } else if (event.getObject() instanceof ReloadTaskEvent || event.getObject() instanceof ReloadEvent) {
                logger.debug("Task reload event.");
                if (event.getObject() instanceof ReloadTaskEvent
                        && ((ReloadTaskEvent) event.getObject()).getTask() != null)
                    worklistHandler.replaceTaskInWorklist(((ReloadTaskEvent) event.getObject()).getTask());
                else
                    worklistHandler.replaceTaskInWorklist();
            }
        }
    }


    public void onStatingPhaseExitReturn(SelectEvent event) {
        if (event.getObject() instanceof StainingPhaseExitData) {
            logger.debug("Staining phase exit dialog return");
            StainingPhaseExitData data = (StainingPhaseExitData) event.getObject();

            workPhaseHandler.endStainingPhase(data.getTask(), data.isEndStainingPhase(),
                    data.isRemoveFromStainingList(), data.isGoToDiagnosisPhase(), data.isRemoveFromWorklist());

            worklistHandler.replaceTaskInWorklist();

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

            worklistHandler.replaceTaskInWorklist();
            return;
        }
        onDefaultDialogReturn(event);
    }

}
