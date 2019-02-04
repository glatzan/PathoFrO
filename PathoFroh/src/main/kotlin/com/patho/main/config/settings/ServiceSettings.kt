package com.patho.main.config.settings

import com.patho.main.common.PredefinedFavouriteList
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component

@Component()
@ConfigurationProperties(prefix = "patho.taskservice")
public open class ServiceSettings {

    var taskArchiveRules: TaskArchiveRules = TaskArchiveRules()

    public open class TaskArchiveRules {
        lateinit var blockingFavouriteLists: List<PredefinedFavouriteList>
    }
}