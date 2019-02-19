package com.patho.main.service

import com.patho.main.action.UserHandlerAction

interface SpringContextBridgedServices {
    var userHandlerAction: UserHandlerAction
    var associatedContactNotificationService : AssociatedContactNotificationService

}