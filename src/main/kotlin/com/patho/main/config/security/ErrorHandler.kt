package com.patho.main.config.security

import com.patho.main.model.user.HistoUser
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.RedirectStrategy
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest

/**
 * Error Handler, catches errors if files are not found
 */
@Controller
class ErrorHandler : ErrorController {

    private val redirectStrategy: RedirectStrategy = DefaultRedirectStrategy()

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest): String? {

        // if url is / redirect to start view
        if (request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI) == "/") {
            val user = SecurityContextHolder.getContext().authentication.principal as HistoUser
            try {
                val startView = user!!.settings.startView
                return "redirect:${startView.rootPath}";
            } catch (e: Exception) {
            }
        }

        // else show error
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        if (status != null) {
            val statusCode = Integer.valueOf(status.toString())
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-404"
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-500"
            }
        }
        return "error"
    }

    override fun getErrorPath(): String? {
        return "/error"
    }
}
