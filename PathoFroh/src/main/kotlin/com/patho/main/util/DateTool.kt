package com.patho.main.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DateTool {

    fun format(pattern: String?, localDate: LocalDate?): String {
        return localDate?.format(DateTimeFormatter.ofPattern(pattern)) ?: ""
    }

    fun format(pattern: String?, date: Date): String {
        return SimpleDateFormat(pattern).format(date) ?: ""
    }
}