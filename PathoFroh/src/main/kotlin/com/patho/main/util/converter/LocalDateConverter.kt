package com.patho.main.util.converter

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.faces.component.UIComponent
import javax.faces.context.FacesContext
import javax.faces.convert.FacesConverter

/**
 * Converter for primefaces calendar and localDate
 */
@FacesConverter("localDateConverter")
class LocalDateConverter : CalendarConverter<LocalDate>() {
    override fun getAsObject(context: FacesContext, component: UIComponent, value: String?): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern(extractPattern(component, context))
        try {
            val result = LocalDate.parse(value, formatter)
            return result;
        }catch (e : DateTimeParseException){
            return null
        }
    }

    override fun getAsString(context: FacesContext, component: UIComponent, date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern(extractPattern(component, context)))
    }
}
