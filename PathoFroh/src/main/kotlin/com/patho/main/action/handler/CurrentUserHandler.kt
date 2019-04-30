package com.patho.main.action.handler

import com.patho.main.model.user.HistoPermissions
import com.patho.main.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Controller
import javax.annotation.PostConstruct

/**
 * Controller encapsulating current functions
 */
@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
open class CurrentUserHandler @Autowired constructor(
        private val userService: UserService) : AbstractHandler() {

    /**
     * Method is executed after object creation, will initialize the transient user settings
     */
    @PostConstruct
    override fun loadHandler() {
        userService.initializeTransientSettings()
    }

    /**
     * Returns true if the current user is available
     */
    open val isCurrentUserAvailable
        get() = userService.isCurrentUserAvailable

    /**
     * Returns the current user
     */
    open val currentUser
        get() = userService.currentUser

    /**
     * Returns true if the user has the passed permissions
     */
    open fun userHasPermission(vararg permissions: HistoPermissions): Boolean {
        return userService.userHasPermission()
    }

    /**
     * Returns an sets the current printer
     */
    open var printer
        get() = currentUser.transient.selectedPrinter
        set(value) {
            currentUser.transient.selectedPrinter = value
        }

    /**
     * Returns an sets the current label printer
     */
    open var labelPrinter
        get() = currentUser.transient.selectedLabelPrinter
        set(value) {
            currentUser.transient.selectedLabelPrinter = value
        }
}