package com.patho.main.util.ui.selector

abstract class UISelector<T>(var item: T) {
    open var id: Long = 0
    open var selected: Boolean = false
}