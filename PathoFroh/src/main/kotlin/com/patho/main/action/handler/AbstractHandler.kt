package com.patho.main.action.handler

import com.patho.main.config.util.ResourceBundle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractHandler {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    protected lateinit var resourceBundle: ResourceBundle

    /**
     * Initializes the current view
     */
    abstract fun loadHandler()
}