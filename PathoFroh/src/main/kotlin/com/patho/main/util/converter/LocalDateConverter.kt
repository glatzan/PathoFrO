package com.patho.main.util.converter

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.faces.component.UIComponent
import javax.faces.context.FacesContext
import javax.faces.convert.Converter
import javax.faces.convert.FacesConverter

@FacesConverter("localDateConverter")
class LocalDateConverter : Converter<LocalDate> {

    override fun getAsObject(context: FacesContext, component: UIComponent, value: String?): LocalDate {
        return LocalDate.parse(value);
    }

    override fun getAsString(context: FacesContext, component: UIComponent, date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
}