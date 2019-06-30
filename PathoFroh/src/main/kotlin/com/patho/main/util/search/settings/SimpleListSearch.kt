package com.patho.main.util.search.settings

import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.patient.Patient
import com.patho.main.service.impl.SpringContextBridge
import java.time.*
import kotlin.collections.HashMap

open class SimpleListSearch : SearchSettings {

    var lists: Array<out PredefinedFavouriteList> = arrayOf(PredefinedFavouriteList.NotificationList, PredefinedFavouriteList.DiagnosisList, PredefinedFavouriteList.StainingList, PredefinedFavouriteList.ReDiagnosisList, PredefinedFavouriteList.ReStainingList, PredefinedFavouriteList.StayInDiagnosisList, PredefinedFavouriteList.StayInStainingList, PredefinedFavouriteList.StayInNotificationList, PredefinedFavouriteList.ScannList)

    var selectedLists: Array<out PredefinedFavouriteList>? = null

    var newPatients: Boolean = false

    var day: LocalDate = LocalDate.now()

    var searchFrom: LocalDate = LocalDate.now()

    var searchTo: LocalDate = LocalDate.now()

    var month: Month = LocalDate.now().month

    var year: Int = LocalDate.now().year

    var years: MutableMap<String, Int> = HashMap<String, Int>(20)

    lateinit var simpleSearchOption: SimpleListSearchOption

    var simpleListSearchCriterion: SimpleListSearchCriterion? = SimpleListSearchCriterion.TaskCreated

    init {
        val cYear = LocalDate.now().year
        for (i in 0 until 20) {
            val dYear = LocalDate.ofYearDay(cYear - i, 1)
            years.put(dYear.year.toString(), dYear.year)
        }

        years = years.toSortedMap()
    }

    constructor()

    constructor(settings: SimpleListSearchOption) {
        updateWithSearchOptions(settings)
    }

    fun updateWithSearchOptions() {
        updateWithSearchOptions(simpleSearchOption)
    }

    fun updateWithSearchOptions(settings: SimpleListSearchOption) {
        logger.debug("Updating search options")
        simpleSearchOption = settings
        selectedLists = settings.lists
        newPatients = settings.newPatient
    }

    override fun getPatients(): List<Patient> {
        logger.debug("Creating criterion for worklist")

        val result = mutableListOf<Patient>()
        val rep = SpringContextBridge.services().patientRepository

        // utc because database is in utc
        val date = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))

        when (simpleSearchOption) {
            SimpleListSearchOption.STAINING_LIST, SimpleListSearchOption.DIAGNOSIS_LIST, SimpleListSearchOption.NOTIFICATION_LIST, SimpleListSearchOption.CUSTOM_LIST -> {
                logger.debug("List selected")
                println(date.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC))

                if (newPatients)
                    result.addAll(rep.findByDateAndCriterion(SimpleListSearchCriterion.NoTasks,
                            date.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                            date.plusDays(1).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                            true, false, true))

                val lists = selectedLists
                if (!lists.isNullOrEmpty()) {
                    result.addAll(rep.findAllByFavouriteLists(
                            (lists.toList().map { it.id }),
                            true))
                }
            }
            SimpleListSearchOption.TODAY -> {
                logger.debug("Today selected")
                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        date.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        date.plusDays(1).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            SimpleListSearchOption.YESTERDAY -> {
                logger.debug("Yesterday selected")
                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        date.minusDays(1).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        date.toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            SimpleListSearchOption.CURRENTWEEK -> {
                logger.debug("Current week selected")
                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        date.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        date.plusDays(7).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            SimpleListSearchOption.LASTWEEK -> {
                logger.debug("Last week selected")
                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        date.minusDays(7).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        date.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            SimpleListSearchOption.CURRENTMONTH -> {
                logger.debug("Current month selected")
                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        date.withDayOfMonth(1).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        date.plusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            SimpleListSearchOption.LASTMONTH -> {
                logger.debug("Last month selected")
                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        date.minusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        date.plusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            SimpleListSearchOption.DAY -> {
                logger.debug("Day selected")

                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        day.atStartOfDay().toInstant(ZoneOffset.UTC),
                        day.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            SimpleListSearchOption.MONTH -> {
                logger.debug("Month selected")
                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        LocalDate.of(year, month.value, 1).atStartOfDay().toInstant(ZoneOffset.UTC),
                        LocalDate.of(year, month.value, 1).plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            SimpleListSearchOption.TIME -> {
                logger.debug("Time selected")
                result.addAll(rep.findByDateAndCriterion(simpleListSearchCriterion,
                        searchFrom.atStartOfDay().toInstant(ZoneOffset.UTC),
                        searchTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                        true, false, true))
            }
            else -> {
            }
        }

        return result
    }
}