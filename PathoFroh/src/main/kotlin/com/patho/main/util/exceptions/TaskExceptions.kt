package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge

/**
 * Exception is thrown if task is not in database
 */
class TaskNotFoundException : DialogException("Task not found",
        SpringContextBridge.services().resourceBundle["exceptions.taskNotFound.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.taskNotFound.text"] ?: "")

/**
 * Exception is thrown if slide is not in database
 */
class SlideNotFoundException : DialogException("Slide not found",
        SpringContextBridge.services().resourceBundle["exceptions.slideNotFound.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.slideNotFound.text"] ?: "")