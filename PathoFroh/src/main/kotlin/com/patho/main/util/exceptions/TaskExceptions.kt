package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge


class TaskNotFoundException : DialogException("Task not found",
        SpringContextBridge.services().resourceBundle["exceptions.taskNotFound.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.taskNotFound.text"] ?: "")