package com.patho.main.util.task

import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.Task

public class TaskArchiveStatus(task: Task) {

    /**
     * True if Task.stainingCompleted is a valid timestamp
     */
    var stainingPhaseCompleted: Boolean = false

    /**
     * True if all slides are marked as completed
     */
    var stainingCompleted: Boolean = false


    var diagnosisPhaseCompleted: Boolean = false
    var notificationPhaseCompleted: Boolean = false
    var councilCompleted: Boolean = false
    var blockingFavouriteLists: List<FavouriteList> = ArrayList<FavouriteList>()

    val isFavouriteListBlocking: Boolean
        get() = blockingFavouriteLists.isEmpty()

}