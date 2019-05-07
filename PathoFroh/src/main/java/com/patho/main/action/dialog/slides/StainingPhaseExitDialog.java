
package com.patho.main.action.dialog.slides;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.common.PredefinedFavouriteList;
import com.patho.main.model.patient.Task;
import com.patho.main.service.FavouriteListService;
import com.patho.main.util.dialogReturn.DialogReturnEvent;
import com.patho.main.util.task.TaskStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Getter
@Setter
public class StainingPhaseExitDialog extends AbstractDialog {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private FavouriteListService favouriteListService;

    private StainingPhaseExitData data;

    /**
     * If true the task will be removed from worklist
     */
    private boolean removeFromWorklist;

    /**
     * If true the task will be removed from staining list
     */
    private boolean removeFromStainingList;

    /**
     * If true the staining phase of the task will be finished
     */
    private boolean endStainingPhase;

    /**
     * If true the task will be shifted to the diagnosis phase
     */
    private boolean goToDiagnosisPhase;

    /**
     * Initializes the bean and shows the dialog
     */
    public StainingPhaseExitDialog initAndPrepareBean(Task task) {
        return initAndPrepareBean(task, false);
    }

    public StainingPhaseExitDialog initAndPrepareBean(Task task, boolean autoCompleteStainings) {
        if (initBean(task, autoCompleteStainings))
            prepareDialog();
        return this;
    }

    /**
     * Initializes all field of the object
     *
     * @param task
     */
    public boolean initBean(Task task, boolean autoCompleteStainings) {

        data = new StainingPhaseExitData();

        data.setTask(task);

        if (TaskStatus.hasFavouriteLists(task, PredefinedFavouriteList.NotificationList))
            data.goToDiagnosisPhase = false;
        else
            data.goToDiagnosisPhase = true;

        // all slides will be marked as completed by endStainingphase methode
        if (autoCompleteStainings) {
            data.setRemoveFromStainingList(true);
            data.setRemoveFromWorklist(true);
            data.setEndStainingPhase(true);
        } else {
            boolean stainingCompleted = TaskStatus.checkIfStainingCompleted(task);

            data.setRemoveFromStainingList(stainingCompleted);
            data.setRemoveFromWorklist(stainingCompleted);
            data.setEndStainingPhase(stainingCompleted);
        }

        //data.setGoToDiagnosisPhase(true);

        return super.initBean(task, Dialog.STAINING_PHASE_EXIT);
    }

    public void exitPhaseAndClose() {
        hideDialog(data);
    }

    @Getter
    @Setter
    public static class StainingPhaseExitData implements DialogReturnEvent {

        /**
         * Current Task
         */
        private Task task;


    }
}
