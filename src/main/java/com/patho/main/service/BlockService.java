package com.patho.main.service;

import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Task;
import com.patho.main.model.preset.StainingPrototype;
import com.patho.main.repository.jpa.BlockRepository;
import com.patho.main.repository.jpa.TaskRepository;
import com.patho.main.util.task.TaskTreeTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     */
    public Task createBlock(Sample sample) {
        return createBlock(sample, true, true, false);
    }

    /**
     * Creates a block for the given sample. Adds all slides from the material
     * preset to the block if createSlides is true.
     */
    public Task createBlock(Sample sample, boolean createSlides, boolean naming, boolean save) {
        Block block = new Block();
        block.setParent(sample);
        sample.getBlocks().add(block);

        logger.debug("Creating new block " + block.getBlockID());

        if (createSlides && sample.getMaterialPreset() != null) {
            for (StainingPrototype proto : sample.getMaterialPreset().getStainingPrototypes()) {
                slideService.createSlide(proto, block, "", "", false, false, false, false);
            }
        }

        if (naming)
            TaskTreeTools.updateNamesInTree(block.getParent(), sample.getTask().getUseAutoNomenclature(), false);

        // saving task
        if (save)
            return taskRepository.save(block.getTask(), resourceBundle.get("log.task.block.new", block));
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

        parent.getBlocks().forEach(p -> TaskTreeTools.updateNamesInTree(p, p.getTask().getUseAutoNomenclature(), false));

        if (save) {
            t = taskRepository.save(block.getTask(), resourceBundle.get("log.task.block.deleted", block));
            blockRepository.delete(block, resourceBundle.get("log.task.block.deleted", block));
        }

        return t;
    }

}
