package com.patho.main.util.search.settings

import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.Patient
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.task.TaskStatus

class FavouriteListSearch : SearchSettings() {

    var favouriteList: FavouriteList? = null

    override fun getPatients(): List<Patient> {
        val rep = SpringContextBridge.services().patientRepository

        val list = favouriteList
        return if (list != null) {
            val res = rep.findAllByFavouriteList(list.id, true)
            res.forEach {
                it.tasks.forEach {
                    it.active = TaskStatus.hasFavouriteLists(it, list)
                }
            }
            res
        } else
            listOf()
    }
}