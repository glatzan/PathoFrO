package com.patho.main.dialog.search

import com.patho.main.common.ContactRole
import com.patho.main.common.Dialog
import com.patho.main.dialog.AbstractTabDialog_
import com.patho.main.model.Physician
import com.patho.main.repository.FavouriteListRepository
import com.patho.main.repository.PhysicianRepository
import com.patho.main.service.UserService
import com.patho.main.ui.FavouriteListContainer
import com.patho.main.ui.transformer.DefaultTransformer
import com.patho.main.util.dialog.event.WorklistSelectEvent
import com.patho.main.util.search.settings.ExtendedSearch
import com.patho.main.util.search.settings.FavouriteListSearch
import com.patho.main.util.search.settings.SearchSettings
import com.patho.main.util.search.settings.SimpleListSearch
import com.patho.main.util.worklist.Worklist
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component()
@Scope(value = "session")
open class SearchWorklistDialog @Autowired constructor(
        private val userService: UserService,
        private val favouriteListRepository: FavouriteListRepository,
        private val physicianRepository: PhysicianRepository) : AbstractTabDialog_(Dialog.WORKLIST_SEARCH) {

    val simpleSearchTab: SimpleSearchTab = SimpleSearchTab()
    val favouriteSearchTab: FavouriteSearchTab = FavouriteSearchTab()
    val extendedSearchTab: ExtendedSearchTab = ExtendedSearchTab()

    init {
        tabs = arrayOf(simpleSearchTab, favouriteSearchTab, extendedSearchTab)
    }

    override fun initBean(): Boolean {
        logger.debug("Initializing search worklist dialog")
        return super.initBean(true, simpleSearchTab)
    }

    open inner class SearchTab<T : SearchSettings>(tabName: String, name: String, viewID: String, centerInclude: String)
        : AbstractTabDialog_.AbstractTab(tabName, name, viewID, centerInclude) {

        lateinit var search: T

        private fun getWorklist(): Worklist {
            return Worklist("Default", search,
                    userService.currentUser.settings.worklistHideNoneActiveTasks,
                    userService.currentUser.settings.worklistSortOrder,
                    userService.currentUser.settings.worklistAutoUpdate, false,
                    userService.currentUser.settings.worklistSortOrderAsc)
        }

        fun selectAndHide() {
            hideDialog(WorklistSelectEvent(getWorklist()))
        }
    }

    open inner class SimpleSearchTab : SearchTab<SimpleListSearch>(
            "SimpleSearchTab",
            "dialog.worklistsearch.simple",
            "simpleSearch",
            "include/simpleSearch.xhtml") {

        override fun initTab(force: Boolean): Boolean {
            search = SimpleListSearch(userService.currentUser.settings.worklistToLoad)
            return super.initTab(force)
        }
    }

    open inner class FavouriteSearchTab : SearchTab<FavouriteListSearch>(
            "FavouriteSearchTab",
            "dialog.worklistsearch.favouriteList",
            "favouriteListSearch",
            "include/favouriteSearch.xhtml") {

        var containers: List<FavouriteListContainer> = listOf()

        var selectedContainer: FavouriteListContainer? = null
            set(value) {
                field = value
                if (value != null)
                    search.favouriteList = value.favouriteList
            }


        override fun initTab(force: Boolean): Boolean {
            search = FavouriteListSearch()
            selectedContainer = null
            return super.initTab(force)
        }

        override fun updateData() {
            val list = favouriteListRepository.findByUserAndWriteableAndReadable(
                    userService.currentUser, false, true, false, true, true, false)
            containers = list.map { FavouriteListContainer(it, userService.currentUser) }
        }


    }

    open inner class ExtendedSearchTab : SearchTab<ExtendedSearch>(
            "ExtendedSearchTab",
            "dialog.worklistsearch.scifi",
            "extendedSearch",
            "include/extendedSearch.xhtml") {

        lateinit var allPhysicians: List<Physician>
        lateinit var allPhysiciansTransformer : DefaultTransformer<Physician>

        lateinit var signaturePhysicians: List<Physician>
        lateinit var signaturePhysiciansTransformer: DefaultTransformer<Physician>

        override fun initTab(force: Boolean): Boolean {
            search = ExtendedSearch()
            allPhysicians = physicianRepository.findAllByRole(arrayOf<ContactRole>(ContactRole.CLINIC_PHYSICIAN, ContactRole.EXTERNAL_SURGEON, ContactRole.FAMILY_PHYSICIAN, ContactRole.OTHER_PHYSICIAN, ContactRole.SURGEON, ContactRole.SIGNATURE), true)
            allPhysiciansTransformer = DefaultTransformer(allPhysicians)

            signaturePhysicians = physicianRepository.findAllByRole(arrayOf<ContactRole>(ContactRole.SIGNATURE), true)
            signaturePhysiciansTransformer = DefaultTransformer(signaturePhysicians)
            return super.initTab(force)
        }

    }


}