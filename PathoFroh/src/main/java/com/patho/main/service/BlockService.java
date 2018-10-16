package com.patho.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.BlockRepository;
import com.patho.main.repository.TaskRepository;

@Service
@Transactional
public class BlockService extends AbstractService {

	@Autowired
	private SlideService slideService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private BlockRepository blockRepository;

	public Task createBlockAndPersist(Sample sample) {
		return createBlock(sample, true, true, true);
	}

	public Task createBlockAndPersist(Sample sample, boolean createSlides, boolean naming) {
		return createBlock(sample, createSlides, naming, true);
	}

	/**
	 * Creates a block for the given sample. Adds all slides from the material
	 * preset to the block.
	 * 
	 * @param sample
	 * @param material
	 */
	public Task createBlock(Sample sample) {
		return createBlock(sample, true, true, false);
	}

	/**
	 * Creates a block for the given sample. Adds all slides from the material
	 * preset to the block if createSlides is true.
	 * 
	 * @param sample
	 * @param material
	 */
	public Task createBlock(Sample sample, boolean createSlides, boolean naming, boolean save) {
		Block block = new Block();
		block.setParent(sample);
		sample.getBlocks().add(block);

		logger.debug("Creating new block " + block.getBlockID());

		if (createSlides && sample.getMaterialPreset() != null) {
			for (StainingPrototype proto : sample.getMaterialPreset().getStainingPrototypes()) {
				slideService.createSlide(proto, block, "", false, false, false, false);
			}
		}

		if (naming)
			block.getParent().updateAllNames(sample.getTask().isUseAutoNomenclature(), false);

		// saving task
		if (save)
			return taskRepository.save(block.getTask(), resourceBundle.get("log.task.blok.new", block));
		return block.getTask();
	}

	/**
	 * Deletes a block and saves the task
	 * 
	 * @param block
	 */
	public Task deleteBlockAndPersist(Block block) {
		return deleteBlock(block, true);
	}

	/**
	 * Deletes a block
	 * 
	 * @param block
	 * @return
	 */
	public Task deleteBlock(Block block, boolean save) {
		Task t = block.getTask();
		Sample parent = block.getParent();

		parent.getBlocks().remove(block);

		parent.getBlocks().forEach(p -> p.updateAllNames());

		if (save) {
			t = taskRepository.save(block.getTask(), resourceBundle.get("log.task.blok.deleted", block));
			blockRepository.delete(block, resourceBundle.get("log.task.blok.deleted", block));
		}

		return t;
	}

	/**
	 * Completes Staining, returns true if a change was made
	 * 
	 * @param slide
	 * @param completed
	 * @return
	 */
	public boolean completedStaining(Block block, boolean completed) {

		boolean changed = false;

		for (Slide slide : block.getSlides()) {
			if (slideService.completedStaining(slide, completed))
				changed = true;
		}

		return changed;
	}

}
