package com.patho.main.service

import com.patho.main.action.handler.WorkPhaseHandler

interface SpringSessionContextBridgedServices {
    var workPhaseHandler: WorkPhaseHandler
}