package com.patho.main.service.impl

import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.action.views.GenericViewData
import com.patho.main.config.util.ApplicationContextProvider
import com.patho.main.service.SpringSessionContextBridgedServices
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
 * Register this SpringContextBridge as a Spring Component. This is for non manged object to get spring beans
 */
@Component
class SpringSessionContextBridge : SpringSessionContextBridgedServices {

    @Autowired
    override lateinit var workPhaseHandler: WorkPhaseHandler

    @Autowired
    override lateinit var genericViewData: GenericViewData

    companion object {
        @JvmStatic
        fun services(): SpringSessionContextBridgedServices {
            return ApplicationContextProvider.getContext().getBean(SpringSessionContextBridgedServices::class.java)
        }
    }
}