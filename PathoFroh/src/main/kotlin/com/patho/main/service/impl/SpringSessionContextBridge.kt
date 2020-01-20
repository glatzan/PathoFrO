package com.patho.main.service.impl

import com.patho.main.action.handler.CentralHandler
import com.patho.main.action.handler.CurrentUserHandler
import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.action.handler.WorklistHandler
import com.patho.main.config.PathoConfig
import com.patho.main.config.util.ApplicationContextProvider
import com.patho.main.config.util.ResourceBundle
import com.patho.main.repository.*
import com.patho.main.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import javax.persistence.EntityManager


/**
 * Register this SpringContextBridge as a Spring Component. This is for non manged object to get spring beans
 */
@Component
class SpringSessionContextBridge : SpringSessionContextBridgedServices {

    @Autowired
    override lateinit var workPhaseHandler: WorkPhaseHandler

    companion object {
        @JvmStatic
        fun services(): SpringSessionContextBridgedServices {
            return ApplicationContextProvider.getContext().getBean(SpringSessionContextBridgedServices::class.java)
        }
    }
}