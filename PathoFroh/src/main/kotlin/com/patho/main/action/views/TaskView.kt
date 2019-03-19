package com.patho.main.action.views

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "session")
class TaskView : AbstractView() {
    override fun loadView() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}