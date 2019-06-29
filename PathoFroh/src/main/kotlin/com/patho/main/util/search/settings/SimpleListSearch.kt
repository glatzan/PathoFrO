package com.patho.main.util.search.settings

import com.patho.main.common.Month
import com.patho.main.common.PredefinedFavouriteList
import com.patho.main.model.favourites.FavouriteList
import com.patho.main.model.patient.Patient
import com.patho.main.model.patient.Task
import com.patho.main.repository.service.PatientRepositoryCustom
import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.helper.TimeUtil
import org.apache.commons.collections.map.LinkedMap
import org.slf4j.LoggerFactory
import java.time.*
import java.util.*
import java.util.stream.Collectors
import javax.persistence.criteria.JoinType
import kotlin.collections.HashMap

open class SimpleListSearch : SearchSettings {

    var lists: Array<out PredefinedFavouriteList> = arrayOf()

    var selectedLists: Array<PredefinedFavouriteList>? = null

    var newPatients: Boolean = false

    var day: LocalDate = LocalDate.now()

    var searchFrom: LocalDate = LocalDate.now()

    var searchTo: LocalDate = LocalDate.now()

    var month: LocalDate = LocalDate.now()

    var year: Int = LocalDate.now().year

    var years: MutableMap<String, Int> = HashMap<String, Int>(20)

    lateinit var simpleSearchOption: SimpleListSearchOption

    var simpleListSearchCriterion: SimpleListSearchCriterion = SimpleListSearchCriterion.StainingCompleted

    init {
        val cYear = LocalDate.now().year
        for (i in 0 until 20) {
            val dYear = LocalDate.ofYearDay(cYear - i, 1)
            years.put(dYear.year.toString(), dYear.year)
        }
    }

    constructor()

    constructor(settings: SimpleListSearchOption) {
        updateWithSearchOptions(settings)
    }

    fun updateWithSearchOptions(settings: SimpleListSearchOption) {
        simpleSearchOption = settings
        lists = settings.lists
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
                        month.atStartOfDay().withDayOfMonth(1).toInstant(ZoneOffset.UTC),
                        month.atStartOfDay().plusMonths(1).withDayOfMonth(1).toInstant(ZoneOffset.UTC),
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