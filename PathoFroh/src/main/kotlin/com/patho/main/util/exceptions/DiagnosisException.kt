package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge

/**
 * Exception which is thrown if the diagnosis phase should be ended and not all diagnoses are completed.
 */
class DiagnosisRevisionsNotCompletedException : DialogException("Diagnoses not completed",
        SpringContextBridge.services().resourceBundle["exceptions.diagnosisRevisionNotCompleted.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.diagnosisRevisionNotCompleted.text"] ?: "")

/**
 * Last diagnosis can not be deleted
 */
class LastDiagnosisCanNotBeDeletedException : DialogException("Diagnoses not completed",
        SpringContextBridge.services().resourceBundle["exceptions.lastDiagnosisCanNotBeDeleted.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.lastDiagnosisCanNotBeDeleted.text"] ?: "")

/**
 * Diagnosis revision not found
 */
class DiagnosisRevisionNotFoundException : DialogException("Diagnoses not found",
        SpringContextBridge.services().resourceBundle["exceptions.diagnosisRevisionNotFound.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.diagnosisRevisionNotFound.text"] ?: "")