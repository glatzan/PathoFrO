package com.patho.main.util.logging

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.html.HTMLLayout

class HTMLLayoutWithUserContext : HTMLLayout() {
    init {
        PatternLayout.defaultConverterMap["user"] = UserConverter::class.java.name
    }
}

