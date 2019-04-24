package com.patho.main.service

import com.patho.main.action.handler.CentralHandler
import com.patho.main.action.handler.CurrentUserHandler
import com.patho.main.config.PathoConfig
import com.patho.main.config.util.ResourceBundle
import com.patho.main.repository.FavouriteListRepository
import com.patho.main.repository.ListItemRepository
import com.patho.main.repository.MediaRepository

/**
 * Briding service for getting spring beans within non spring managed objects
 */
interface SpringContextBridgedServices {
    var currentUserHandler: CurrentUserHandler
    var reportIntentService: ReportIntentService
    var resourceBundle: ResourceBundle
    var organizationService: OrganizationService
    var reportService: ReportService
    var pathoConfig: PathoConfig
    var mediaRepository: MediaRepository
    var physicianService: PhysicianService
    var listItemRepository: ListItemRepository
    var userService: UserService
    var favouriteListRepository: FavouriteListRepository
    var centralHandler: CentralHandler
}