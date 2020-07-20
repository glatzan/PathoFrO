package com.patho.main.util.converter

import org.primefaces.component.calendar.Calendar
import javax.faces.component.UIComponent
import javax.faces.context.FacesContext
import javax.faces.convert.Converter

abstract class CalendarConverter<T> : Converter<T> {
    /**
     *  try to get infos from calendar component
     */
    protected open fun extractPattern(component: UIComponent, context: FacesContext): String {
        // try to get infos from calendar component
        if (component is Calendar) {
            val calendarComponent = component as Calendar
            return calendarComponent.getPattern()
        }

        return ""
    }
}