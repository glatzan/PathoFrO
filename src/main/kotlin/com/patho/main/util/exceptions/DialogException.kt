package com.patho.main.util.exceptions

open class DialogException : Throwable {

    val guiHeadline: String
    val guiText: String

    constructor(causeString: String) : this(causeString, "", "")

    constructor(causeString: String, guiHeadline: String, guiText: String) : super(causeString) {
        this.guiHeadline = guiHeadline
        this.guiText = guiText
    }

}