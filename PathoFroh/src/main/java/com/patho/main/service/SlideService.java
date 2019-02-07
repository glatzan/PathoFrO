package com.patho.main.service;

import com.patho.main.model.StainingPrototype;
import com.patho.main.model.patient.Block;
import com.patho.main.model.patient.Sample;
import com.patho.main.model.patient.Slide;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.SlideRepository;
import com.patho.main.repository.TaskRepository;
import com.patho.main.ui.selectors.StainingPrototypeHolder;
import com.patho.main.util.task.TaskTreeTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SlideService extends AbstractService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SlideRepository slideRepository;

    @Autowired
    private StainingPrototypeService stainingPrototypeService;

    public void createSlideAndPersist(StainingPrototype prototype, Block block) {
        createSlide(prototype, block, null, null, false, true, false, true);
    }

    public void createSlideAndPersist(StainingPrototype prototype, Block block, String slideLabelText,
                                      boolean reStaining, boolean naming, boolean asCompleted) {
        createSlide(prototype, block, slideLabelText, null, reStaining, naming, asCompleted, true);
    }

    /**
     * Creates a list of slides
     */
    public Task createSlidesAndPersist(List<StainingPrototype> prototypes, Block block) {
        return createSlidesAndPersist(prototypes, block, null, false, true, false);
    }

    /**
     * Creates a lists of slides
     */
    public Task createSlidesAndPersist(List<StainingPrototype> prototypes, Block block, String slideLabelText,
                                       boolean reStaining, boolean naming, boolean asCompleted) {

        for (StainingPrototype p : prototypes) {
            createSlide(p, block, slideLabelText, null, reStaining, naming, asCompleted, false);
        }

        return taskRepository.save(block.getTask(), resourceBundle.get("log.task.slide.new", block));
    }

    /**
     * Creates a list of slides, does persist data
     */
    public Task createSlidesXTimesAndPersist(List<StainingPrototypeHolder> prototypeHolders, Block block) {
        return createSlidesXTimesAndPersist(prototypeHolders, block, null, null, false, true, false);
    }

    /**
     * Creates a lists of slides, does persist data
     */
    public Task createSlidesXTimesAndPersist(List<StainingPrototypeHolder> prototypeHolders, Block block,
                                             String slideLabelText, String commentary, boolean reStaining, boolean naming, boolean asCompleted) {
        return createSlidesXTimes(prototypeHolders, block, slideLabelText, commentary, reStaining, naming, asCompleted,
                true);
    }

    /**
     * Creats a slide x times, does not persist data
     */
    public Task createSlidesXTimes(List<StainingPrototypeHolder> prototypeHolders, Block block) {
        return createSlidesXTimes(prototypeHolders, block, null, null, false, true, false, false);
    }

    /**
     * Creats a slide x times
     */
    public Task createSlidesXTimes(List<StainingPrototypeHolder> prototypeHolders, Block block, String slideLabelText,
                                   String commentary, boolean reStaining, boolean naming, boolean asCompleted, boolean save) {

        for (StainingPrototypeHolder p : prototypeHolders) {
            for (int i = 0; i < p.getCount(); i++)
                createSlide(p.getPrototype(), block, slideLabelText, commentary, reStaining, naming, asCompleted,
                        false);
        }

        if (save)
            return taskRepository.save(block.getTask(), resourceBundle.get("log.task.slide.newxtimes", block));
        return block.getTask();
    }

    /**
     * Adds a new staining to a block. Sets the staining completion time to 0.
     */
    public Task createSlide(StainingPrototype prototype, Block block) {
        return createSlide(prototype, block, null, null, false, true, false, false);
    }

    /**
     * Adds a new staining to a block. Task has to be saved in order to store the
     * unique slide id counter
     */
    public Task createSlide(StainingPrototype prototype, Block block, String slideLabelText, String commentary,
                            boolean reStaining, boolean naming, boolean asCompleted, boolean save) {

        logger.debug("Creating new slide " + prototype.getName());

        Slide slide = new Slide();

        slide.setCreationDate(System.currentTimeMillis());
        slide.setSlidePrototype(stainingPrototypeService.incrementPriorityCounter(prototype));
        slide.setParent(block);

        // setting unique slide number
        slide.setUniqueIDinTask(block.getTask().getNextSlideNumber());

        block.getSlides().add(slide);

        if (naming)
            TaskTreeTools.updateNamesInTree(slide.getParent(), block.getTask().getUseAutoNomenclature(), false);

        slide.setSlideLabelText(slideLabelText);
        slide.setCommentary(commentary);
        slide.setReStaining(reStaining);

        if (asCompleted) {
            slide.setCompletionDate(System.currentTimeMillis());
            slide.setStainingCompleted(true);
        }

        // saving task, slide is saved as well and the unqiue slide id counter is
        // updated
        if (save)
            return taskRepository.save(slide.getTask(), resourceBundle.get("log.task.slide.new", slide));
        return slide.getTask();
    }

    /**
     * Deletes a slide and saves the task
     *
     * @param slide
     * @return
     */
    public Task deleteSlideAndPersist(Slide slide) {
        return deleteSlide(slide, true);
    }

    /**
     * Deletes a slide
     *
     * @param slide
     * @return
     */
    public Task deleteSlide(Slide slide, boolean save) {
        Task t = slide.getTask();
        Block parent = slide.getParent();

        parent.getSlides().remove(slide);


        parent.getSlides().forEach(p -> TaskTreeTools.updateNamesInTree(p, p.getTask().getUseAutoNomenclature(), false));

        if (save) {
            t = taskRepository.save(slide.getTask(), resourceBundle.get("log.task.slide.deleted", slide));
            slideRepository.delete(slide, resourceBundle.get("log.task.slide.deleted", slide));
        }

        return t;
    }

    /**
     * Completes Staining, returns true if a change was made
     *
     * @param task
     * @param completed
     * @return
     */
    public boolean completedStaining(Task task, boolean completed) {

        boolean changed = false;

        for (Sample sample : task.getSamples()) {
            if (completedStaining(sample, completed))
                changed = true;
        }

        return changed;
    }

    /**
     * Completes Staining, returns true if a change was made
     *
     * @param sample
     * @param completed
     * @return
     */
    public boolean completedStaining(Sample sample, boolean completed) {

        boolean changed = false;

        for (Block block : sample.getBlocks()) {
            if (completedStaining(block, completed))
                changed = true;
        }

        return changed;
    }

    /**
     * Completes Staining, returns true if a change was made
     *
     * @param block
     * @param completed
     * @return
     */
    public boolean completedStaining(Block block, boolean completed) {

        boolean changed = false;

        for (Slide slide : block.getSlides()) {
            if (completedStaining(slide, completed))
                changed = true;
        }

        return changed;
    }

    /**
     * Completes Staining, returns true if a change was made
     */
    public boolean completedStaining(Slide slide, boolean completed) {

        if (slide.getStainingCompleted() != completed) {
            slide.setStainingCompleted(completed);
            slide.setCompletionDate(System.currentTimeMillis());
            return true;
        }

        return false;
    }

    /**
     * Set a slide staining as completed and saves the updated status
     */
    public Task completedStainingAndPersist(Task task, boolean completed) {
        if (completedStaining(task, completed))
            return taskRepository.save(task, resourceBundle.get("log.task.slide.completed", task));
        return task;
    }

    /**
     * Set a slide staining as completed and saves the updated status
     *
     * @param slide
     * @param completed
     * @return
     */
    public Task completedStainingAndPersist(Slide slide, boolean completed) {
        if (completedStaining(slide, completed))
            return taskRepository.save(slide.getTask(), resourceBundle.get("log.task.slide.completed", slide));
        return slide.getTask();
    }
}
