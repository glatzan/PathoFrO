package com.patho.main.template

/**
 * U_REPORT = Eingangsbogen print at blank page <br>
 * U_REPORT_EMTY = Eingangsbogen print at template, only infill of missing date  <br>
 * U_REPORT_COMPLETED = A completed Eingangsbogen with handwriting <br>
 * DIAGNOSIS_REPORT = Report for internal diagnoses, multiple can exist for printing <br>
 * DIAGNOSIS_REPORT_EXTERN = Report for external diagnoses, multiple can exist for printing
 * LABLE = Labels for printing with zebra printers <br>
 * BIOBANK_INFORMED_CONSENT = media of informed consent for biobank <br>
 * CASE_CONFERENCE = media and printing of case confernces <br>
 * OTHER
 *
 * @author andi
 */
enum class PrintDocumentType {
    /**
     * Document with unknown type
     */
    UNKNOWN,

    /**
     * document for printing with patient data an document
     */
    U_REPORT,

    /**
     * document for printing with only patient data, no document data
     */
    U_REPORT_EMTY,

    /**
     * document with filled out fields, this is uploaded by the user
     */
    U_REPORT_COMPLETED,
    /**
     *
     */
    DIAGNOSIS_REPORT,
    DIAGNOSIS_REPORT_COMPLETED, DIAGNOSIS_REPORT_NOT_APPROVED, DIAGNOSIS_REPORT_EXTERN, LABLE,
    BIOBANK_INFORMED_CONSENT, TEST_LABLE, COUNCIL_REQUEST, COUNCIL_REPLY, OTHER, EMPTY,

    /**
     * Sendreport, generated by the system
     */
    NOTIFICATION_SEND_REPORT,

    /**
     *
     */
    MEDICAL_FINDINGS_SEND_REPORT_COMPLETED,

    /**
     * Document for printing
     */
    PRINT_DOCUMENT;

    companion object {
        /**
         * Returns an enum Object if the given string matches the name of the enum
         */
        @JvmStatic
        fun fromString(text: String): PrintDocumentType {
            for (b in PrintDocumentType.values()) {
                if (b.name.equals(text, ignoreCase = true)) {
                    return b
                }
            }
            throw IllegalArgumentException("No constant with text $text found")
        }
    }
}