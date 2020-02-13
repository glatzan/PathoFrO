package com.patho.main.service

import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.config.PathoConfig
import com.patho.main.model.patient.Slide
import com.patho.main.model.patient.Task
import com.patho.main.model.scanner.ScannedSlide
import com.patho.main.model.scanner.ScannedTask
import com.patho.main.repository.jpa.ScannedTaskRepository
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.rest.data.ScannedSlideData
import com.patho.main.template.DocumentToken
import com.patho.main.util.exceptions.EntityNotFoundException
import com.patho.main.util.exceptions.EntityPersistException
import com.patho.main.util.exceptions.SlideNotFoundException
import com.patho.main.util.exceptions.TaskNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
open class ScannedTaskService @Autowired constructor(
        private val scannedTaskRepository: ScannedTaskRepository,
        private val taskRepository: TaskRepository,
        private val mailService: MailService,
        private val pathoConfig: PathoConfig,
        private val favouriteListService: FavouriteListService) : AbstractService() {

    /**
     * Gets a slide from the database by caseID an unique slideID
     */
    @Throws(SlideNotFoundException::class, IllegalArgumentException::class, TaskNotFoundException::class)
    open fun getSlideFromUniqueID(caseID: String?, uniqueSlideID: String?): Slide {
        if (caseID == null || uniqueSlideID == null)
            throw IllegalArgumentException("No caseID or slideID provided")

        val task = taskRepository.findByTaskID(caseID)

        return task.samples.flatMap { it.blocks.flatMap { it.slides } }.find { it.uniqueIDinTask.toString() == uniqueSlideID }
                ?: throw SlideNotFoundException()
    }

    @Throws(TaskNotFoundException::class)
    open fun addScannedSlidesToTask(caseID: String, slides: Array<ScannedSlideData>): ScannedTask {

        var task = taskRepository.findByTaskID(caseID)

        // adding to scanned list
        task = favouriteListService.addTaskToList(task, PredefinedFavouriteList.ScannCompletedList)

        val scannedTask = updateScannedTask(task, slides)

        return scannedTaskRepository.save(scannedTask, resourceBundle["log.scannedSlide.added", caseID], scannedTask.task.patient)
    }

    private fun updateScannedTask(task: Task, slides: Array<ScannedSlideData>): ScannedTask {
        val scannedTask = scannedTaskRepository.findOptionalByIdAndInitializeTask(task.id).orElse(ScannedTask(task))
        val notFoundList = mutableListOf<ScannedSlideData>()

        logger.debug("Updating scanned slides for ${task.taskID}")

        for (slide in slides) {

            // if slide was already added skip
            if (!scannedTask.slides.any { it.name == slide.name }) {
                logger.debug("Scanned slide $slide found, do nothing")
                continue
            }

            val slidesList = task.samples.flatMap { it.blocks.flatMap { it.slides } }

            // checking if unique id matches
            val foundSlide = slidesList.firstOrNull { it.uniqueIDinTask == slide.uniqueSlideID }

            if (foundSlide == null) {
                notFoundList.add(slide)
                logger.debug("Could not find matching slide to name: $slide, adding without match")
            } else {
                logger.debug("Matching slide found: $slide, adding with match")
            }

            val scannedSlide = ScannedSlide()
            scannedSlide.name = slide.name
            scannedSlide.slideID = foundSlide?.id ?: 0
            scannedTask.slides.add(scannedSlide)
        }

        // notify if failed or wanted
        if (notFoundList.isNotEmpty() || pathoConfig.miscellaneous.noticeAdminOnScannedSlideAdded)
            mailService.sendAdminMail(pathoConfig.defaultDocuments.scannedSlidesAdded,
                    DocumentToken("taskID", task.taskID),
                    DocumentToken("notFoundSlides", notFoundList),
                    DocumentToken("slides", slides),
                    DocumentToken("scannedSlide", scannedTask.slides))

        return scannedTaskRepository.save(scannedTask, resourceBundle["log.scannedSlide.removed", scannedTask.task.taskID], scannedTask.task.parent)
    }

    @Throws(EntityNotFoundException::class)
    open fun removeScannedSlideFromTask(caseID: String, uniqueSlideID: String): ScannedTask {
        val scannedTask = scannedTaskRepository.findOptionalByIdAndInitializeTask(caseID.toLong())

        if(!scannedTask.isPresent)
            throw EntityNotFoundException()

        scannedTask.get().slides.firstOrNull { it.slideID ==  }
    }

    /**
     * Removes a scannedSlide from the scanned Task table. Saves data to the database.
     */
    @Throws(EntityPersistException::class)
    open fun removeScannedSlideFromTask(scannedTask: ScannedTask, scannedSlide: ScannedSlide): ScannedTask {
        val result = scannedTask.slides.remove(scannedSlide)

        if (!result)
            throw EntityPersistException()

        return scannedTaskRepository.save(scannedTask, resourceBundle["log.scannedSlide.removed", scannedTask.task.taskID, scannedSlide.slideID], scannedTask.task.parent)
    }
}