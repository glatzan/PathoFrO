package com.patho.main.rest.data

import java.io.Serializable

/**
 * Post data for adding new scanned slides
 */
class ScannedSlideData : Serializable {
    var name: String = ""
    var uniqueSlideID: Int = 0
}