package com.patho.main.service.impl

import com.patho.main.action.UserHandlerAction
import com.patho.main.config.util.ApplicationContextProvider
import com.patho.main.config.util.ResourceBundle
import com.patho.main.service.OrganizationService
import com.patho.main.service.ReportIntentService
import com.patho.main.service.SpringContextBridgedServices
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
 * Register this SpringContextBridge as a Spring Component. This is for non manged object to get spring beans
 */
@Component
class SpringContextBridge : SpringContextBridgedServices {
    @Autowired
    override lateinit var reportIntentService: ReportIntentService

    @Autowired
    override lateinit var userHandlerAction: UserHandlerAction

    @Autowired
    override lateinit var  resourceBundle : ResourceBundle

    override lateinit var organizationService : OrganizationService

    companion object {
        @JvmStatic
        fun services(): SpringContextBridgedServices {
            return ApplicationContextProvider.getContext().getBean(SpringContextBridgedServices::class.java)
        }
    }
}