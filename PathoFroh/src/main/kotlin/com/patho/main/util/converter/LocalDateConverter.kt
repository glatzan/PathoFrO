package com.patho.main.util.converter

import org.primefaces.component.calendar.Calendar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.faces.component.UIComponent
import javax.faces.context.FacesContext
import javax.faces.convert.Converter
import javax.faces.convert.FacesConverter

/**
 * Converter for primefaces calendar and localDate
 */
@FacesConverter("localDateConverter")
class LocalDateConverter : Converter<LocalDate> {

    override fun getAsObject(context: FacesContext, component: UIComponent, value: String?): LocalDate {
        val formatter = DateTimeFormatter.ofPattern(extractPattern(component, context))

        return LocalDate.parse(value, formatter);
    }

    override fun getAsString(context: FacesContext, component: UIComponent, date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern(extractPattern(component, context)))
    }

    /**
     *  try to get infos from calendar component
     */
    private fun extractPattern(component: UIComponent, context: FacesContext): String? {
        // try to get infos from calendar component
        if (component is Calendar) {
            val calendarComponent = component as Calendar
            return calendarComponent.getPattern()
        }

        return null
    }
}
