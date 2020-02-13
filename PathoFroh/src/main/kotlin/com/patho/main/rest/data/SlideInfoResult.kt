package com.patho.main.rest.data

import com.patho.main.model.patient.Slide
import java.io.Serializable

/**
 * Result for slideinfo request
 */
class SlideInfoResult constructor(val slide : Slide) : Serializable{
    val name = slide.slideID
    val uniqueID = slide.uniqueIDinTask
}