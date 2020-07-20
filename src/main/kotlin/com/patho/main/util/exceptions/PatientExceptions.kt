package com.patho.main.util.exceptions

import com.patho.main.service.impl.SpringContextBridge

class PatientNotFoundException : DialogException("Patient not found",
        SpringContextBridge.services().resourceBundle["exceptions.patientNotFound.headline"] ?: "",
        SpringContextBridge.services().resourceBundle["exceptions.patientNotFound.text"] ?: "")