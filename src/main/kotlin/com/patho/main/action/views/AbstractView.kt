package com.patho.main.action.views

import com.patho.main.config.util.ResourceBundle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractView {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    protected lateinit var resourceBundle: ResourceBundle

    /**
     * Initializes the current view
     */
    abstract fun loadView()
}