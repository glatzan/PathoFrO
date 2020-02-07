package com.patho.main.util.logging

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

class UserConverter : ClassicConverter() {
    override fun convert(p0: ILoggingEvent?): String {
        val auth: Authentication? = SecurityContextHolder.getContext().authentication
        return if (auth != null) {
            auth.name
        } else "NO_USER"
    }
}