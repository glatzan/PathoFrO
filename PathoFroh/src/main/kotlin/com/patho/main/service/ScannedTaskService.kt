package com.patho.main.service

import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.config.PathoConfig
import com.patho.main.model.patient.Task
import com.patho.main.model.scanner.ScannedSlide
import com.patho.main.model.scanner.ScannedTask
import com.patho.main.repository.jpa.ScannedTaskRepository
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.template.DocumentToken
import com.patho.main.util.exceptions.TaskNotFoundException
import kotlinx.coroutines.delay
import org.springframework.beans.factory.annotation.Autowired

class ScannedTaskService @Autowired constructor(
        private val scannedTaskRepository: ScannedTaskRepository,
        private val taskRepository: TaskRepository,
        private val mailService: MailService,
        private val pathoConfig: PathoConfig,
        private val favouriteListService: FavouriteListService) : AbstractService() {

    fun addScannedSlidesToTask(caseID: String, slides: Array<String>) {

        if (slides.isEmpty()) {
            logger.error("No scanned slides for task $caseID provided ")
            return
        }

        var task = try {
            taskRepository.findByTaskID(caseID)
        } catch (e: TaskNotFoundException) {
            return
        }

        // adding to scanned list
        task = favouriteListService.addTaskToList(task, PredefinedFavouriteList.ScannCompletedList)

        val scannedTask = updateScannedTask(task, slides)

        scannedTaskRepository.save(scannedTask, resourceBundle["log.scannedSlide.added", caseID])
    }

    private fun updateScannedTask(task: Task, slides: Array<String>): ScannedTask {
        val scannedTask = scannedTaskRepository.findById(task.id).orElse(ScannedTask(task))
        val notFoundList = mutableListOf<String>()

        logger.debug("Updating scanned slides for ${task.taskID}")

        for (slide in slides) {
            if (scannedTask.slides.any { it.name == slide }) {
                logger.debug("Scanned slide $slide found, do nothing")
                continue
            }

            val slides = task.samples.flatMap { it.blocks.flatMap { it.slides } }
            val foundSlide = slides.firstOrNull { it.slideID == slide }

            if (foundSlide == null) {
                notFoundList.add(slide)
                logger.debug("Could not find matching slide to name: $slide, adding without match")
            } else {
                logger.debug("Matching slide found: $slide, adding with match")
            }

            val scannedSlide = ScannedSlide()
            scannedSlide.name = slide
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

        return scannedTask
    }
}