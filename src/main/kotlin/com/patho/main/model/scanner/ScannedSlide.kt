package com.patho.main.model.scanner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
open class ScannedSlide : Serializable, Cloneable {
    var name : String = ""
    var slideID : Long = 0
    var path : String = ""
}