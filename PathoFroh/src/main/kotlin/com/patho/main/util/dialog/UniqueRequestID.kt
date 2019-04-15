package com.patho.main.util.dialog

import com.patho.main.util.exception.CustomNotUniqueReqest
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.security.SecureRandom
import javax.faces.context.FacesContext

class UniqueRequestID {

    protected val logger = LoggerFactory.getLogger(this.javaClass)

    private val SUBMITTEDID = "submittedRequestID"

    private val random = SecureRandom()


    var uniqueRequestID: String = ""

    var enabled: Boolean = false

    /**
     * Generates the next unique id
     */
    fun nextUniqueRequestID() {
        uniqueRequestID = BigInteger(130, random).toString(32)
        logger.debug("New Unique ID generated -> $uniqueRequestID")
    }

    /**
     * Checks if the request id matches
     */
    fun checkUniqueRequestID(toClose: Boolean) {
        // disables uniqueCheck
        checkUniqueRequestID(toClose, false)
    }

    /**
     * Checks if the request id matches
     */
    fun checkUniqueRequestID(toClose: Boolean, enabled: Boolean) {

        val fc = FacesContext.getCurrentInstance()
        val params = fc.externalContext.requestParameterMap
        val submittedRequestID = params[SUBMITTEDID]

        if (submittedRequestID == null || submittedRequestID.isEmpty()) {
            logger.debug("No ID submitted")
            throw CustomNotUniqueReqest(toClose)
        }

        if (uniqueRequestID.isEmpty() || uniqueRequestID != submittedRequestID) {
            logger.debug("ID does not equal")
            throw CustomNotUniqueReqest(toClose)
        }

        logger.debug("Unique ID matched")

        uniqueRequestID = ""
        this.enabled = enabled
    }
}