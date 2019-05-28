package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.exception.DialogException


class StainingsNotCompletedException : DialogException("Stainigns not completed",
        SpringContextBridge.services().resourceBundle["exceptions.stainingsNotCompleted.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.stainingsNotCompleted.text"] ?: "")