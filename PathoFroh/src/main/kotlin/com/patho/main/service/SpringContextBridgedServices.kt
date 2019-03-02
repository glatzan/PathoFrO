package com.patho.main.service

import com.patho.main.action.UserHandlerAction
import com.patho.main.config.util.ResourceBundle

interface SpringContextBridgedServices {
    var userHandlerAction: UserHandlerAction
    var reportIntentService : ReportIntentService
    var resourceBundle : ResourceBundle
    var organizationService : OrganizationService
    var reportService : ReportService
}