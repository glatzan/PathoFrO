package com.patho.main.service

import com.patho.main.action.handler.WorkPhaseHandler
import com.patho.main.action.views.GenericViewData

interface SpringSessionContextBridgedServices {
    var workPhaseHandler: WorkPhaseHandler
    var genericViewData: GenericViewData
}