package com.patho.main.template

/**
 * Different Mailtypes
 */
enum class MailType(val templateName: String) {

    /**
     * Error Mail
     */
    ERROR_MAIL("error.mail"),

    /**
     * Request unlock
     */
    REQUEST_UNLOCK_MAIL("unlock.mail"),

    /**
     * Success unlock
     */
    SUCCESS_UNLOCK_MAIL("unlockOk.mail"),

    /**
     * Diagnosis report
     */
    DIAGNOSIS_REPORT_MAIL("diagnosisReport.mail");

    companion object {
        /**
         * Returns an enum Object if the given string matches the name of the enum
         */
        @JvmStatic
        fun fromString(text: String): MailType {
            for (b in MailType.values()) {
                if (b.name.equals(text, ignoreCase = true)) {
                    return b
                }
            }
            throw IllegalArgumentException("No constant with text $text found")
        }
    }
}