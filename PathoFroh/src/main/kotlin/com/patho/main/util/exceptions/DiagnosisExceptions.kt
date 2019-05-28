package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.exception.DialogException


class DiagnosisRevisionNotFoundException : DialogException("Task not found",
        SpringContextBridge.services().resourceBundle["exceptions.diagnosisRevisionNotFound.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.diagnosisRevisionNotFound.text"] ?: "")