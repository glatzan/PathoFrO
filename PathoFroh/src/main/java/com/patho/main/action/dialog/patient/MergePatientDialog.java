package com.patho.main.action.dialog.patient;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.action.handler.MessageHandler;
import com.patho.main.common.Dialog;
import com.patho.main.model.patient.Patient;
import com.patho.main.model.patient.Task;
import com.patho.main.service.impl.SpringContextBridge;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialog.event.ConfirmEvent;
import com.patho.main.util.dialog.event.PatientMergeEvent;
import com.patho.main.util.dialog.event.PatientReloadEvent;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DualListModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class MergePatientDialog extends AbstractDialog {

    public static final int MERGE_PIZ = 0;
    public static final int MERGE_PATIENT = 1;

    private Patient source;

    private Patient target;

    private DualListModel<Task> taskLists = new DualListModel<Task>();

    private DefaultTransformer<Task> taskTransformer;

    private boolean taskPickerDisabled;

    private boolean samePatientForSourceAndTarget;

    private boolean taskWasMoved;

    private boolean sourceDeleteAble;

    private boolean targetDeleteAble;

    private boolean deleteSource;

    private boolean deleteTarget;

    public MergePatientDialog initAndPrepareBean() {
        return initAndPrepareBean((Patient) null);
    }

    public MergePatientDialog initAndPrepareBean(Patient source) {
        return initAndPrepareBean(source, null);
    }

    public MergePatientDialog initAndPrepareBean(Patient source, Patient target) {
        if (initBean(source, target))
            prepareDialog();
        return this;
    }

    public boolean initBean(Patient source, Patient target) {
        setSource(source);
        setTarget(target);

        updateOnChange();

        return super.initBean(null, Dialog.PATIENT_MERGE);
    }

    public void onReturnSelectSource(SelectEvent event) {
        if (event.getObject() != null && event.getObject() instanceof PatientReloadEvent) {
            Patient patient = ((PatientReloadEvent) event.getObject()).getPatient();
            // Reloading patient with tasks
            setSource(patient.getId() != 0 ? SpringContextBridge.services().getPatientRepository().findOptionalById(patient.getId(), true, false).get()
                    : patient);

            updateOnChange();
        }
    }

    public void onReturnSelectTarget(SelectEvent event) {
        if (event.getObject() != null && event.getObject() instanceof PatientReloadEvent) {
            Patient patient = ((PatientReloadEvent) event.getObject()).getPatient();
            // Reloading patient with tasks
            setTarget(patient.getId() != 0 ? SpringContextBridge.services().getPatientRepository().findOptionalById(patient.getId(), true, false).get()
                    : patient);

            updateOnChange();
        }
    }

    public void updateOnChange() {
        taskLists.setSource(source == null ? new ArrayList<Task>() : new ArrayList<Task>(source.getTasks()));
        taskLists.setTarget(target == null ? new ArrayList<Task>() : new ArrayList<Task>(target.getTasks()));

        taskTransformer = new DefaultTransformer<Task>(Stream.of(taskLists.getSource(), taskLists.getTarget())
                .flatMap(Collection::stream).collect(Collectors.toList()));

        if (getSource() != null && getTarget() != null && getSource().equals(getTarget())) {
            samePatientForSourceAndTarget = true;
        } else
            samePatientForSourceAndTarget = false;

        setTaskWasMoved(false);

        setSourceDeleteAble(getSource() != null && taskLists.getSource().size() == 0);
        setTargetDeleteAble(getTarget() != null && taskLists.getTarget().size() == 0);

        setTaskPickerDisabled((source == null || target == null || samePatientForSourceAndTarget));
    }

    public void onTaskMoved() {
        setTaskWasMoved(true);
    }

    /**
     * If merging was confirmed this method will merge the two patients
     *
     * @param event
     */
    public void onMergePatientReturn(SelectEvent event) {
        if (event.getObject() != null && event.getObject() instanceof ConfirmEvent
                && ((ConfirmEvent) event.getObject()).getObj()) {
            if (source != null && target != null) {
                SpringContextBridge.services().getPatientService().moveTaskBetweenPatients(source, target, taskLists.getSource(), taskLists.getTarget());

                if (deleteSource)
                    source = SpringContextBridge.services().getPatientService().archive(source, true);

                if (deleteTarget)
                    target = SpringContextBridge.services().getPatientService().archive(target, true);

                MessageHandler.sendGrowlMessagesAsResource("growl.success", "growl.patient.merge.success");
                hideDialog(new PatientMergeEvent(source, target));
            }
        }
    }
}