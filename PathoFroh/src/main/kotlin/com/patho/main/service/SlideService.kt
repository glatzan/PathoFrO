package com.patho.main.service

import com.patho.main.model.StainingPrototype
import com.patho.main.model.patient.Block
import com.patho.main.model.patient.Sample
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.repository.SlideRepository
import com.patho.main.repository.TaskRepository
import com.patho.main.ui.selectors.StainingPrototypeHolder
import com.patho.main.util.task.TaskTreeTools
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class SlideService @Autowired constructor(
        private val stainingPrototypeService: StainingPrototypeService,
        private val taskRepository: TaskRepository,
        private val slideRepository: SlideRepository
) : AbstractService() {

    /**
     * Creates a lists of slides for a block
     */
    @Transactional
    open fun createSlides(stainingPrototypes: List<StainingPrototype>, block: Block, labelText: String = "",
                          commentary: String = "", isRestaining: Boolean = false, autoNaming: Boolean = true,
                          completed: Boolean = false, save: Boolean = true): Task {
        stainingPrototypes.forEach { createSlide(it, block, labelText, commentary, isRestaining, autoNaming, completed, false) }

        return if (save) taskRepository.save<Task>(block.task, resourceBundle.get("log.task.slide.new", block.task, block), block.patient) else block.task!!
    }

    /**
     * Creates a lists of slides nth times
     */
    @Transactional
    open fun createSlidesXTimes(holders: List<StainingPrototypeHolder>, block: Block, labelText: String = "",
                                commentary: String = "", isRestaining: Boolean = false, autoNaming: Boolean = true,
                                completed: Boolean = false, save: Boolean = true): Task {
        holders.forEach {
            for (i in 0 until it.count)
                createSlide(it, block, labelText, commentary, isRestaining, autoNaming, completed, false)
        }

        return if (save) taskRepository.save<Task>(block.task, resourceBundle.get("log.task.slide.new", block.task, block), block.patient) else block.task!!
    }

    /**
     * Creates a slide
     */
    @Transactional
    open fun createSlide(stainingPrototype: StainingPrototype, block: Block, labelText: String = "",
                         commentary: String = "", isRestaining: Boolean = false, autoNaming: Boolean = true,
                         completed: Boolean = false, save: Boolean = true): Task {

        logger.debug("Creating new slide " + stainingPrototype.getName())

        val slide = Slide()

        slide.creationDate = System.currentTimeMillis()
        slide.slidePrototype = stainingPrototypeService.incrementPriorityCounter(stainingPrototype)
        slide.parent = block

        // setting unique slide number
        slide.uniqueIDinTask = block.task!!.getNextSlideNumber()

        block.slides.add(slide)

        if (autoNaming)
            TaskTreeTools.updateNamesInTree(slide.parent!!, block.task!!.useAutoNomenclature, false)

        slide.slideLabelText = labelText
        slide.commentary = commentary
        slide.reStaining = isRestaining

        if (completed) {
            slide.completionDate = System.currentTimeMillis()
            slide.stainingCompleted = true
        }

        // saving task, slide is saved as well and the unqiue slide id counter is // updated
        return if (save) taskRepository.save<Task>(slide.task, resourceBundle.get("log.task.slide.new", slide.task, slide), block.patient) else slide.task!!
    }

    /**
     * Deletes a slide
     */
    @Transactional
    open fun deleteSlide(slide: Slide, save: Boolean = true): Task {
        var t = slide.task
        val parent = slide.parent


        parent!!.slides.remove(slide)
        parent.slides.forEach { p -> TaskTreeTools.updateNamesInTree(p, p.task!!.useAutoNomenclature, false) }

        if (save) {
            t = taskRepository.save<Task>(slide.task, resourceBundle.get("log.task.slide.deleted", slide.task, slide), slide.patient)
            slideRepository.delete(slide, resourceBundle.get("log.task.slide.deleted", slide.task, slide), slide.patient)
        }

        return t!!
    }

    /**
     * Completes the staining of a slide
     */
    @Transactional
    open fun completeStaining(task: Task, complete: Boolean = true, save: Boolean = true): Task {
        task.samples.forEach { completeStaining(it, false) }

        if (save)
            return taskRepository.save(task, resourceBundle.get("log.task.slide.completedTask", task, task), task.patient)
        return task
    }

    /**
     * Completes the staining of a slide
     */
    @Transactional
    open fun completeStaining(sample: Sample, complete: Boolean = true, save: Boolean = true): Task {
        sample.blocks.forEach { completeStaining(it, false) }

        if (save)
            return taskRepository.save(sample.task!!, resourceBundle.get("log.task.slide.completedSample", sample.task, sample), sample.patient)
        return sample.task!!
    }

    /**
     * Completes the staining of a slide
     */
    @Transactional
    open fun completeStaining(block: Block, complete: Boolean = true, save: Boolean = true): Task {
        block.slides.forEach { completeStaining(it, false) }

        if (save)
            return taskRepository.save(block.task!!, resourceBundle.get("log.task.slide.completedBlock", block.task, block), block.patient)
        return block.task!!
    }

    /**
     * Completes the staining of a slide
     */
    @Transactional
    open fun completeStaining(slide: Slide, complete: Boolean = true, save: Boolean = true): Task {
        slide.stainingCompleted = complete
        slide.completionDate = if (complete) System.currentTimeMillis() else 0

        if (save)
            return taskRepository.save(slide.task!!, resourceBundle.get("log.task.slide.completedSlide", slide.task, slide), slide.patient)
        return slide.task!!
    }

}