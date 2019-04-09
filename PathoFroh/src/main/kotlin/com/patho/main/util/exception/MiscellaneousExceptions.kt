package com.patho.main.util.exception

import com.patho.main.service.impl.SpringContextBridge

/**
 * Is thrown if a contact is about to be added twice to a task
 */
class DuplicatedReportIntentException : DialogException("Contact already in list",
        SpringContextBridge.services().resourceBundle["exceptions.reportintent.duplicated.headline"]
                ?: "", SpringContextBridge.services().resourceBundle["exceptions.reportintent.duplicated.info"] ?: "")