package com.patho.main.template

import java.beans.ConstructorProperties

class DocumentToken {
    val key: String
    val value: Any?

    @ConstructorProperties("key", "value")
    constructor(key: String, value: Any?) {
        this.key = key
        this.value = value
    }
}