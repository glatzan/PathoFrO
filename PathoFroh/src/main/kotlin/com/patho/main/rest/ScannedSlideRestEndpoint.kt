package com.patho.main.rest

import com.google.gson.Gson
import com.patho.main.action.handler.AbstractHandler
import com.patho.main.repository.jpa.TaskRepository
import com.patho.main.rest.data.ScannedSlideData
import com.patho.main.rest.data.SlideInfoResult
import com.patho.main.service.ScannedTaskService
import com.patho.main.util.exceptions.EntityNotFoundException
import com.patho.main.util.exceptions.SlideNotFoundException
import com.patho.main.util.exceptions.TaskNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(value = ["/rest/slide"])
open class ScannedSlideRestEndpoint @Autowired constructor(
        private val taskRepository: TaskRepository,
        private val scannedTaskService: ScannedTaskService) : AbstractHandler() {

    override fun loadHandler() {
    }

    @RequestMapping(value = ["/add"], method = [RequestMethod.POST])
    open fun handelSlideScanAdd(@RequestParam(value = "caseID", required = false) caseID: String = "",
                                @RequestParam(value = "slides", required = false) slides: Array<ScannedSlideData> = emptyArray()): String {

        logger.debug("Post with $caseID -> ${slides.joinToString { "$it " }}")

        try {
            scannedTaskService.addScannedSlidesToTask(caseID, slides)
        } catch (e: TaskNotFoundException) {
            return resourceBundle["rest.scannedSlides.taskNoFound", caseID]
        } catch (e: IllegalArgumentException) {
            return resourceBundle["rest.scannedSlides.noslide"]
        }

        return resourceBundle["rest.scannedSlides.added.success", caseID]
    }

    /**
     * Returns the slide name if taskID and uniqueSlideId is provided
     */
    @RequestMapping(value = ["/info"], method = [RequestMethod.GET])
    open fun handleSlideInfoRequest(@RequestParam caseID: String?, @RequestParam uniqueSlideID: String?): String {

        if (caseID == null || uniqueSlideID == null)
            return resourceBundle["rest.scannedSlides.requestNotValid"]

        return try {
            val slide = scannedTaskService.getSlideFromUniqueID(caseID, uniqueSlideID)
            Gson().toJson(SlideInfoResult(slide))
        } catch (e: TaskNotFoundException) {
            resourceBundle["rest.scannedSlides.taskNoFound", caseID]
        } catch (e: SlideNotFoundException) {
            resourceBundle["rest.scannedSlides.noslide"]
        }
    }

    /**
     * Removes a slide from the scanned slide list
     */
    @RequestMapping(value = ["/remove"], method = [RequestMethod.GET])
    open fun handleScannedSlideRemoveRequest(@RequestParam caseID: String?, @RequestParam slideName: String?): String {

        if (caseID == null || slideName == null)
            return resourceBundle["rest.scannedSlides.requestNotValid"]

        return try {
            scannedTaskService.removeScannedSlideFromTask(caseID, slideName)
            resourceBundle["rest.scannedSlide.removeSuccess"]
        }catch (e : EntityNotFoundException){
            resourceBundle["rest.scannedSlide.removeFailed"]
        }
    }
}