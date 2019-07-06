package com.patho.main.util.search.settings

import com.patho.main.common.ContactRole
import com.patho.main.common.Eye
import com.patho.main.model.Physician
import com.patho.main.model.Signature
import com.patho.main.model.StainingPrototype
import com.patho.main.model.patient.*
import com.patho.main.model.patient.notification.ReportIntent
import com.patho.main.model.person.Person
import com.patho.main.util.helper.HistoUtil
import org.hibernate.Session
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.stream.Collectors
import javax.persistence.EntityManager
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Predicate

abstract class SearchSettings {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    abstract fun getPatients() : List<Patient>

    abstract fun getTasks() : List<Task>
}