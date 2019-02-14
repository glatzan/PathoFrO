package com.patho.main.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateTool {

    fun format(pattern: String?, localDate: LocalDate?): String {
        return localDate?.format(DateTimeFormatter.ofPattern(pattern)) ?: ""
    }
}