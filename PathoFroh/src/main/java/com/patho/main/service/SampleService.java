package com.patho.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.MaterialPreset;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.SampleRepository;
import com.patho.main.repository.TaskRepository;

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

	/**
	 * Completes Staining, returns true if a change was made
	 * 
	 * @param slide
	 * @param completed
	 * @return
	 */
	public boolean completedStaining(Sample sample, boolean completed) {

		boolean changed = false;

		for (Block block : sample.getBlocks()) {
			if (blockService.completedStaining(block, completed))
				changed = true;
		}

		return changed;
	}

	public void createSampleAndPersist(Task task, MaterialPreset material) {
		createSample(task, material, true, true, true);
	}

	public void createSampleAndPersist(Task task, MaterialPreset material, boolean createBlock, boolean naming) {
		createSample(task, material, createBlock, naming, true);
	}

	/**
	 * Creates an sample, udpates all names and checks the statining phase
	 * 
	 * @param task
	 * @param material
	 */
	public Task createSample(Task task, MaterialPreset material) {
		return createSample(task, material, true, true, true);
	}

	/**
	 * Creates a new sample and adds this sample to the given task. Creates a new
	 * diagnosis and a new block with slides as well.
	 * 
	 * @param task
	 */
	public Task createSample(Task task, MaterialPreset material, boolean createBlock, boolean naming, boolean save) {
		Sample sample = new Sample();
		sample.setCreationDate(System.currentTimeMillis());
		sample.setParent(task);
		sample.setMaterialPreset(material);
		sample.setMaterial(material == null ? "" : material.getName());
		task.getSamples().add(sample);

		logger.debug("Creating new sample " + sample.getSampleID());

		// creating needed blocks
		if (createBlock)
			blockService.createBlock(sample, true, false, false);

		if (naming)
			sample.updateAllNames();

		// creating first default diagnosis
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
		parent.updateAllNames();

		// creating first default diagnosis
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
	public Task updateMaterialOfSampleWithoutPrototype(Sample sample, String name) {
		sample.setMaterialPreset(null);
		return updateMaterialOfSample(sample, name);
	}

	/**
	 * Changes the material of an sample
	 * 
	 * @param sample
	 * @param materialPreset
	 */
	public Task updateMaterialOfSample(Sample sample, MaterialPreset materialPreset) {
		sample.setMaterialPreset(materialPreset);
		return updateMaterialOfSample(sample, materialPreset.getName());
	}

	/**
	 * Changes the material of an sample
	 * 
	 * @param sample
	 * @param materialPreset
	 */
	public Task updateMaterialOfSample(Sample sample, String name) {
		sample.setMaterial(name);
		return taskRepository.save(sample.getTask(),
				resourceBundle.get("log.patient.task.sample.material.update", sample.getTask(), sample, name));
	}
}