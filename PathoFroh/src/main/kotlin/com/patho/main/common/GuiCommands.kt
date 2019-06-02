package com.patho.main.common

/**
 * Gui commands which are executed from beands
 */
class GuiCommands {
    companion object {

        /**
         *  Staining phase exit dialog
         */
        @JvmStatic
        val OPEN_STAINING_PHASE_EXIT_DIALOG: String = "clickButtonFromBean('headerForm:stainingPhaseExit')"

        /**
         * Staining phase exit dialog from staining overview dialog
         */
        @JvmStatic
        val OPEN_STAINING_PHASE_EXIT_DIALOG_FROM_SLIDE_OVERVIEW_DIALOG: String = "clickButtonFromBean('dialogContent:stainingPhaseExitFromDialog')"

        /**
         * Add diagnosis revision form staining overview dialog
         */
        @JvmStatic
        val OPEN_ADD_DIAGNOSIS_REVISION_DIALOG_FROM_SLIDE_OVERVIEW_DIALOG: String = "clickButtonFromBean('dialogContent:addDiagnosisRevisionFromDialog')"

        /**
         * Opens the copy histological record dialog from diagnosis view
         */
        @JvmStatic
        val OPEN_COPY_HISTOLOGICAL_RECORD_DIALOG_FROM_DIAGNOSIS_VIEW: String = "clickButtonFromBean('contentForm:openDiagnosisRecordDialog')"
    }
}