package com.patho.main.rest.data

import java.io.Serializable

/**
 * Post data for adding new scanned slides
 */
class ScannedSlideData : Serializable {
    var name: String = ""
    var uniqueSlideID: Long = 0
    var slideID: Long = 0
    var path : String = ""
}