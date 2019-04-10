package com.patho.main.util.ui.selector

import java.io.Serializable

abstract class UISelector<T>(var item: T) : Serializable {
    open var id: Long = 0
    open var selected: Boolean = false
}