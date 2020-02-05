package com.patho.main.service;

import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.model.preset.MaterialPreset;
import com.patho.main.repository.jpa.SampleRepository;
import com.patho.main.repository.jpa.TaskRepository;
import com.patho.main.util.task.TaskTreeTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SampleService extends AbstractService {

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SampleRepository sampleRepository;

    public void createSampleAndPersist(Task task, MaterialPreset material) {
        createSample(task, material, material == null ? "" : material.getName(), true, true, true);
    }

    public void createSampleAndPersist(Task task, MaterialPreset material, boolean createBlock, boolean naming) {
        createSample(task, material, material == null ? "" : material.getName(), createBlock, naming, true);
    }

    /**
     * Creates an sample, udpates all names and checks the statining phase
     *
     * @param task
     * @param material
     */
    public Task createSample(Task task, MaterialPreset material) {
        return createSample(task, material, material == null ? "" : material.getName(), true, true, true);
    }

    /**
     * Creates a new sample and adds this sample to the given task. Creates a new
     * reportIntent and a new block with slides as well.
     *
     * @param task
     */
    public Task createSample(Task task, MaterialPreset material, String materialName, boolean createBlock,
                             boolean naming, boolean save) {
        return createSample(task, material, materialName, createBlock, true, naming, save);
    }

    /**
     * Creates a new sample and adds this sample to the given task. Creates a new
     * reportIntent and a new block with slides as well.
     *
     * @param task
     */
    public Task createSample(Task task, MaterialPreset material, String materialName, boolean createBlock,
                             boolean createSlides, boolean naming, boolean save) {
        Sample sample = new Sample();
        sample.setCreationDate(System.currentTimeMillis());
        sample.setParent(task);
        sample.setMaterialPreset(material);
        sample.setMaterial(materialName);
        task.getSamples().add(sample);

        logger.debug("Creating new sample " + sample.getSampleID());

        // creating needed blocks
        if (createBlock)
            blockService.createBlock(sample, createSlides, false, false);

        if (naming)
            TaskTreeTools.updateNamesInTree(sample, sample.getTask().getUseAutoNomenclature(), false);

        // creating first default reportIntent
        diagnosisService.synchronizeDiagnosesAndSamples(task, false);

        if (save)
            return taskRepository.save(sample.getTask(), resourceBundle.get("log.task.sample.new", sample));
        return task;

    }

    /**
     * Deletes a sample and updates all names, saves the task
     *
     * @param sampel
     */
    public Task deleteSampleAndPersist(Sample sampel) {
        return deleteSample(sampel, true);
    }

    /**
     * Deletes a sampel
     *
     * @param sampel
     * @param save
     * @return
     */
    public Task deleteSample(Sample sampel, boolean save) {
        logger.debug("Deleting sample (" + sampel.getId() + ")");
        Task parent = sampel.getParent();

        parent.getSamples().remove(sampel);
        TaskTreeTools.updateNamesInTree(parent, parent.getTask().getUseAutoNomenclature(), false);

        // creating first default reportIntent
        diagnosisService.synchronizeDiagnosesAndSamples(parent, false);

        if (save) {
            parent = taskRepository.save(parent, resourceBundle.get("log.task.sample.delete", sampel));
            sampleRepository.delete(sampel, resourceBundle.get("log.task.sample.delete", sampel));
        }

        return parent;
    }

    /**
     * Changes the material of an sample
     *
     * @param sample
     * @param materialPreset
     */
    public Task updateMaterialOfSample(Sample sample, MaterialPreset materialPreset, boolean save) {
        return updateMaterialOfSample(sample, materialPreset.getName(), materialPreset, save);
    }

    /**
     * Changes the material of an sample
     */
    public Task updateMaterialOfSample(Sample sample, String name, MaterialPreset materialPreset, boolean save) {
        sample.setMaterial(name);
        sample.setMaterialPreset(materialPreset);
        if (save)
            return taskRepository.save(sample.getTask(),
                    resourceBundle.get("log.patient.task.sample.material.update", sample.getTask(), sample, name),
                    sample.getTask().getPatient());
        else
            return sample.getTask();
    }
}