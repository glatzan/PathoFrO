package com.patho.main.util.report

import com.patho.main.service.impl.SpringContextBridge
import com.patho.main.util.helper.HistoUtil
import org.apache.commons.validator.routines.EmailValidator

/**
 * Approve methods for checking the validity of addresses for report notifications
 */
class ReportAddressValidator {
    companion object {
        @JvmStatic
        fun approveMailAddress(address: String): Boolean {
            return HistoUtil.isNotNullOrEmpty(address) && EmailValidator.getInstance().isValid(address)
        }

        @JvmStatic
        fun approveFaxAddress(address: String): Boolean {
            return HistoUtil.isNotNullOrEmpty(address) && address.matches(Regex(SpringContextBridge.services().pathoConfig.miscellaneous.phoneRegex))
        }


        @JvmStatic
        fun approvePostalAddress(address: String): Boolean {
            return HistoUtil.isNotNullOrEmpty(address)
        }
    }
}