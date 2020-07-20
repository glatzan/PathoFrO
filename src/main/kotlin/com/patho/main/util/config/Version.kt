package com.patho.main.util.config

import java.util.*

class VersionContainer {

    companion object {

        private val PREFIX_VERSION = "+v "
        private val PREFIX_INFO = "+++"
        private val SUFFIX_FIX = "!"
        private val SUFFIX_CHANGE = "*"
        private val SUFFIX_ADDED = "+"
        private val SUFFIX_REMOVED = "-"

        private val FIX_STR = "<b>Fix: </b>"
        private val CHANGE_STR = "<b>Change: </b>"
        private val ADDED_STR = "<b>Added: </b>"
        private val REMOVED_STR = "<b>Remove: </b>"

        /**
         * Factory loads a list of Version Objects from a json file
         *
         * @param jsonFile
         * @return
         */
        fun factory(fileContent: List<String>): List<Version> {

            val resultArray = ArrayList<Version>()

            var currentVersion: Version? = null

            for (content in fileContent) {
                // if string starts with version prefix, start new version
                if (content.startsWith(PREFIX_VERSION)) {
                    currentVersion = Version(content.substring(3))
                    resultArray.add(currentVersion)
                } else {
                    // if version info
                    if (content.startsWith(PREFIX_INFO) && currentVersion != null) {
                        // check which version info is present

                        if (content.regionMatches(PREFIX_INFO.length, SUFFIX_FIX, 0, SUFFIX_FIX.length)) {
                            currentVersion.changes
                                    .add(FIX_STR + content.substring(PREFIX_INFO.length + SUFFIX_FIX.length + 1))
                        } else if (content.regionMatches(PREFIX_INFO.length, SUFFIX_CHANGE, 0, SUFFIX_CHANGE.length)) {
                            currentVersion.changes
                                    .add(CHANGE_STR + content.substring(PREFIX_INFO.length + SUFFIX_FIX.length + 1))
                        } else if (content.regionMatches(PREFIX_INFO.length, SUFFIX_ADDED, 0, SUFFIX_ADDED.length)) {
                            currentVersion.changes
                                    .add(ADDED_STR + content.substring(PREFIX_INFO.length + SUFFIX_FIX.length + 1))
                        } else if (content.regionMatches(PREFIX_INFO.length, SUFFIX_REMOVED, 0,
                                        SUFFIX_REMOVED.length)) {
                            currentVersion.changes
                                    .add(REMOVED_STR + content.substring(PREFIX_INFO.length + SUFFIX_FIX.length + 1))
                        } else {
                            currentVersion.changes.add(content.substring(PREFIX_INFO.length + 1))
                        }
                    }
                }
            }

            return resultArray
        }
    }

    val versions: MutableList<Version> = mutableListOf()

    var currentVersion: String = ""

    constructor() {
    }

    constructor(content: List<String>) {
        val versions: List<Version> = factory(content)
        this.versions.addAll(versions)

        // setting the current version
        if (this.versions.isNotEmpty())
            currentVersion = versions[0].version
    }

    class Version(val version: String) {
        val changes: MutableList<String> = mutableListOf<String>()
    }
}
