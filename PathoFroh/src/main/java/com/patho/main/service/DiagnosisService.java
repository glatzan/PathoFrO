package com.patho.main.service;

import com.patho.main.action.dialog.diagnosis.CreateDiagnosisRevisionDialog.DiagnosisRevisionContainer;
import com.patho.main.common.DiagnosisRevisionType;
import com.patho.main.common.DiagnosisValidationError;
import com.patho.main.config.util.ResourceBundle;
import com.patho.main.model.DiagnosisPreset;
import com.patho.main.model.Signature;
import com.patho.main.model.patient.Diagnosis;
import com.patho.main.model.patient.DiagnosisRevision;
import com.patho.main.model.patient.DiagnosisRevision.NotificationStatus;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.*;
import com.patho.main.ui.task.DiagnosisReportUpdater;
import com.patho.main.util.exception.CustomUserNotificationExcepetion;
import com.patho.main.util.exception.HistoDatabaseInconsistentVersionException;
import com.patho.main.util.helper.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DiagnosisService extends AbstractService {

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ResourceBundle resourceBundle;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TransactionTemplate transactionTemplate;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TaskRepository taskRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private DiagnosisRevisionRepository diagnosisRevisionRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private PatientRepository patientRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private FavouriteListService favouriteListService;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private MediaRepository mediaRepository;

    @Autowired
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AssociatedContactService associatedContactService;

    /**
     * Updates all diagnosisRevision of the given revisions
     *
     * @param Task task
     */
    public Task synchronizeDiagnosesAndSamples(Task task, boolean save) {
        logger.info("Synchronize all diagnoses of task " + task.getTaskID() + " with samples");

        for (DiagnosisRevision revision : task.getDiagnosisRevisions()) {
            synchronizeDiagnosesAndSamples(revision, task.getSamples(), save);
        }

        if (save)
            return taskRepository.save(task, resourceBundle.get("log.patient.task.diagnosisRevisions.update", task));
        else
            return task;

    }

    /**
     * Updates a diagnosisRevision with a sample list. Used for adding and removing
     * samples after initial revision creation.
     *
     * @param diagnosisRevision
     * @param samples
     */
    public DiagnosisRevision synchronizeDiagnosesAndSamples(DiagnosisRevision diagnosisRevision, List<Sample> samples,
                                                            boolean save) {
        logger.info("Synchronize diagnosis list with samples");

        List<Diagnosis> diagnosesInRevision = diagnosisRevision.getDiagnoses();

        List<Sample> samplesToAddDiagnosis = new ArrayList<Sample>(samples);

        List<Diagnosis> toRemoveDiagnosis = new ArrayList<Diagnosis>();

        outerLoop:
        for (Diagnosis diagnosis : diagnosesInRevision) {
            // sample already in diagnosisList, removing from to add array
            for (Sample sample : samplesToAddDiagnosis) {
                if (sample.getId() == diagnosis.getSample().getId()) {
                    samplesToAddDiagnosis.remove(sample);
                    logger.trace("Sample found, Removing sample " + sample.getId() + " from list.");
                    continue outerLoop;
                }
            }
            logger.trace("Diagnosis has no sample, removing diagnosis " + diagnosis.getId());
            // not found within samples, so sample was deleted, deleting
            // diagnosis as well.
            toRemoveDiagnosis.add(diagnosis);
        }

        // removing diagnose if necessary
        for (Diagnosis diagnosis : toRemoveDiagnosis) {
            removeDiagnosis(diagnosis);
        }

        // adding new diagnoses if there are new samples
        for (Sample sample : samplesToAddDiagnosis) {
            logger.trace("Adding new diagnosis for sample " + sample.getId());
            createDiagnosis(diagnosisRevision, sample, false);
        }
        if (save)
            return diagnosisRevisionRepository.save(diagnosisRevision,
                    resourceBundle.get("log.patient.task.diagnosisRevision.new", diagnosisRevision));
        else
            return diagnosisRevision;
    }

    /**
     * Creates a new diagnosis and adds it to the given diagnosisRevision
     *
     * @param revision
     * @param sample
     * @return
     */
    public Task createDiagnosis(DiagnosisRevision revision, Sample sample, boolean save) {
        logger.info("Creating new diagnosis");

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setSample(sample);
        diagnosis.setParent(revision);
        revision.getDiagnoses().add(diagnosis);

        if (save)
            return taskRepository.save(revision.getTask(),
                    resourceBundle.get("log.patient.task.diagnosisRevision.diagnosis.new", diagnosis));
        else
            return revision.getTask();
    }

    /**
     * Removes a diagnosis from the parent and deletes it.
     *
     * @param diagnosis
     * @return
     */
    public void removeDiagnosis(Diagnosis diagnosis) {
        logger.info("Removing diagnosis " + diagnosis.getName());

        diagnosis.setSample(null);

        diagnosis.getParent().getDiagnoses().remove(diagnosis);

        diagnosisRepository.delete(diagnosis,
                resourceBundle.get("log.patient.task.diagnosisRevision.diagnosis.remove", diagnosis));
    }

    /**
     * Creates a diagnosisRevision, adds it to the given task and creates also all
     * needed diagnoses
     */
    public Task createDiagnosisRevision(Task task, DiagnosisRevisionType type) {
        return createDiagnosisRevision(task, type, null);
    }

    /**
     * Creates a diagnosisRevision, adds it to the given task and creates also all
     * needed diagnoses
     */
    public Task createDiagnosisRevision(Task task, DiagnosisRevisionType type, String interalReference) {
        return createDiagnosisRevision(task, type,
                TaskUtil.getDiagnosisRevisionName(task.getDiagnosisRevisions(), new DiagnosisRevision("", type)),
                interalReference);
    }

    /**
     * Creates a diagnosisRevision, adds it to the given task and creates also all
     * needed diagnoses
     */
    public Task createDiagnosisRevision(Task task, DiagnosisRevisionType type, String name, String interalReference) {
        logger.info("Creating new diagnosisRevision");

        DiagnosisRevision diagnosisRevision = new DiagnosisRevision();
        diagnosisRevision.setType(type);
        diagnosisRevision.setSignatureOne(new Signature());
        diagnosisRevision.setSignatureTwo(new Signature());
        diagnosisRevision.setName(name);

        return addDiagnosisRevision(task, diagnosisRevision);
    }

    /**
     * Adds an diagnosis revision to the task
     */
    @Transactional
    public Task addDiagnosisRevision(Task task, DiagnosisRevision diagnosisRevision) {
        logger.info("Adding diagnosisRevision to task");
        diagnosisRevision.setParent(task);
        diagnosisRevision.setSignatureOne(new Signature());
        diagnosisRevision.setSignatureTwo(new Signature());
        task.getDiagnosisRevisions().add(diagnosisRevision);

        // saving to database
        // diagnosisRevisionRepository.save(diagnosisRevision,
        // resourceBundle.get("log.patient.task.diagnosisRevision.new",
        // diagnosisRevision));

        // creating a diagnosis for every sample
        for (Sample sample : task.getSamples()) {
            task = createDiagnosis(diagnosisRevision, sample, false);
        }

        // saving to database
        return taskRepository.save(task,
                resourceBundle.get("log.patient.task.diagnosisRevision.new", diagnosisRevision));
    }

    /**
     * Deleting a DiagnosisRevision and all included diagnoese
     *
     * @param revision
     * @return
     */
    public Task removeDiagnosisRevision(Task task, DiagnosisRevision revision) throws CustomUserNotificationExcepetion {
        logger.info("Removing diagnosisRevision " + revision.getName());

        if (task.getDiagnosisRevisions().size() > 1) {

            task.getDiagnosisRevisions().remove(revision);

            task = taskRepository.save(revision.getParent(),
                    resourceBundle.get("log.patient.task.diagnosisRevision.delete", revision.toString()));

            diagnosisRevisionRepository.delete(revision);
            return task;
        } else {
            throw new CustomUserNotificationExcepetion("growl.error", "growl.diagnosis.delete.last");
        }
    }

    /**
     * Sets a diangosis as completed
     */
    public Task approveDiangosis(Task task, DiagnosisRevision diagnosisRevision,
                                 NotificationStatus notificationStatus) {

        diagnosisRevision.setCompletionDate(Instant.now());
        diagnosisRevision.setNotificationStatus(notificationStatus);

        task = taskRepository.save(task,
                resourceBundle.get("log.patient.task.diagnosisRevision.approved", diagnosisRevision.getName()));

        generateDefaultDiagnosisReport(task, diagnosisRevision);
        return task;
    }

    /**
     * Generated or updates the default diagnosis report for a diagnosis
     *
     * @param task
     * @param diagnosisRevision
     * @return
     */
    public Task generateDefaultDiagnosisReport(Task task, DiagnosisRevision diagnosisRevision) {
        logger.debug("Generating new report");
        return new DiagnosisReportUpdater().updateDiagnosisReportNoneBlocking(task, diagnosisRevision);
    }

    /**
     * Updates a diagnosis with out a diagnosis prototype
     *
     * @param task
     * @param diagnosis
     * @param diagnosisAsText
     * @param extendedDiagnosisText
     * @param malign
     * @param icd10
     * @return
     */
    public Task updateDiagnosisWithoutPrototype(Task task, Diagnosis diagnosis, String diagnosisAsText,
                                                String extendedDiagnosisText, boolean malign, String icd10) {
        diagnosis.setDiagnosisPrototype(null);

        return updateDiagnosis(task, diagnosis, diagnosisAsText, extendedDiagnosisText, malign, icd10);
    }

    /**
     * Updates a diagnosis with a diagnosispreset.
     *
     * @param task
     * @param diagnosis
     * @param preset
     * @return
     */
    public Task updateDiagnosisWithPrototype(Task task, Diagnosis diagnosis, DiagnosisPreset preset) {
        logger.debug("Updating diagnosis with prototype");

        diagnosis.setDiagnosisPrototype(preset);

        return updateDiagnosis(task, diagnosis, preset.getDiagnosis(), preset.getExtendedDiagnosisText(),
                preset.isMalign(), preset.getIcd10());
    }

    /**
     * Updates a diagnosis with the given data. Also updates notification via letter
     *
     * @param task
     * @param diagnosis
     * @param diagnosisAsText
     * @param extendedDiagnosisText
     * @param malign
     * @param icd10
     * @return
     */
    public Task updateDiagnosis(Task task, Diagnosis diagnosis, String diagnosisAsText, String extendedDiagnosisText,
                                boolean malign, String icd10) {
        logger.debug("Updating diagnosis to " + diagnosisAsText);

        diagnosis.setDiagnosis(diagnosisAsText);
        diagnosis.setMalign(malign);
        diagnosis.setIcd10(icd10);

        // only setting diagnosis text if one sample and no text has been
        // added
        // jet
        if (diagnosis.getParent().getText() == null || diagnosis.getParent().getText().isEmpty()) {
            diagnosis.getParent().setText(extendedDiagnosisText);
            logger.debug("Updating revision extended text");
        }

        // updating all contacts on diagnosis change, an determine if the
        // contact should receive a physical case report
        task = associatedContactService.updateNotificationsForPhysicalDiagnosisReport(task);

        task = taskRepository.save(task, resourceBundle.get("log.patient.task.diagnosisRevision.diagnosis.update", task,
                diagnosis, diagnosis.getDiagnosis()));

        return task;
    }

    public Task renameDiagnosisRevisions(Task task, List<DiagnosisRevisionContainer> containers) {

        boolean changed = false;
        for (DiagnosisRevisionContainer diagnosisRevisionContainer : containers) {
            if (!diagnosisRevisionContainer.getNewName().equals(diagnosisRevisionContainer.getName())) {
                logger.debug("Updating revision name from " + diagnosisRevisionContainer.getName() + " to "
                        + diagnosisRevisionContainer.getName());
                // updating name
                diagnosisRevisionContainer.setName(diagnosisRevisionContainer.getNewName());
                changed = true;
            }
        }

        if (changed)
            task = taskRepository.save(task,
                    resourceBundle.get("log.patient.task.diagnosisRevision.diagnosis.name.update", task));

        return task;
    }

    /**
     * Returns an empty list if the diangosis is valied, if not the errors will be
     * liested
     *
     * @param diagnosisRevision
     * @return
     */
    public List<DiagnosisValidationError> validateDiangosisRevision(DiagnosisRevision diagnosisRevision) {
        ArrayList<DiagnosisValidationError> result = new ArrayList<DiagnosisValidationError>();

        if (diagnosisRevision.getCompletionDate() == null) {
            result.add(DiagnosisValidationError.NO_SIGNATURE_DATE);
        }

        if ((diagnosisRevision.getSignatureOne() == null || diagnosisRevision.getSignatureOne().getPhysician() == null)
                && (diagnosisRevision.getSignatureTwo() == null
                || diagnosisRevision.getSignatureTwo().getPhysician() == null)) {
            result.add(DiagnosisValidationError.NO_SIGANTURE);
        }

        return result;
    }

    /**
     * Copies the histological record
     *
     * @param diagnosis
     * @param overwrite
     * @return
     * @throws HistoDatabaseInconsistentVersionException
     */
    public Task copyHistologicalRecord(Diagnosis diagnosis, boolean overwrite)
            throws HistoDatabaseInconsistentVersionException {

        String text = overwrite ? diagnosis.getDiagnosisPrototype().getExtendedDiagnosisText()
                : diagnosis.getParent().getText() + "\r\n"
                + diagnosis.getDiagnosisPrototype().getExtendedDiagnosisText();

        diagnosis.getParent().setText(text);

        return taskRepository.save(diagnosis.getTask(),
                resourceBundle.get("log.patient.task.diagnosisRevision.update", diagnosis.getDiagnosis()));
    }

    /**
     * Returns the next diagnosisrevision where the completion date is 0, if no
     * diagnosis is found null is returned.
     *
     * @param task
     * @return
     */
    public static DiagnosisRevision getNextRevisionToApprove(Task task) {
        for (DiagnosisRevision diagnosisRevision : task.getDiagnosisRevisions()) {
            if (diagnosisRevision.getCompletionDate() == null) {
                return diagnosisRevision;
            }
        }

        return null;
    }

    /**
     * Counts diagnoses which are not completed jet,
     *
     * @param task
     * @return
     */
    public static int countRevisionToApprove(Task task) {
        int result = 0;
        for (DiagnosisRevision diagnosisRevision : task.getDiagnosisRevisions()) {
            if (diagnosisRevision.getCompletionDate() == null) {
                result++;
            }
        }

        return result;
    }

}
