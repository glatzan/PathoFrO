package com.patho.main.util.ui.selector

import org.slf4j.LoggerFactory
import java.io.Serializable

abstract class UISelector<T>(var item: T, id: Long = 0) : Serializable {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    open var id: Long = 0
    open var selected: Boolean = false
}