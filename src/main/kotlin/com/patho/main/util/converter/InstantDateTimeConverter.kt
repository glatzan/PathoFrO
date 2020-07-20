package com.patho.main.util.converter

import java.time.*
import java.time.format.DateTimeFormatter
import javax.faces.component.UIComponent
import javax.faces.context.FacesContext
import javax.faces.convert.Converter
import javax.faces.convert.FacesConverter

/**
 * Converter for displaying instant in h:outputText
 */
@FacesConverter("instantDateTimeConverter")
class InstantDateTimeConverter : Converter<Instant> {

    override fun getAsObject(context: FacesContext, component: UIComponent, value: String?): Instant {
        val formatter = DateTimeFormatter.ofPattern(getPattern(component))
        val temporalAccessor = formatter.parse(value)
        val localDateTime = LocalDateTime.from(temporalAccessor)
        val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
        val result = Instant.from(zonedDateTime)
        return result;
    }

    override fun getAsString(context: FacesContext, component: UIComponent, date: Instant?): String {
        val o = OffsetDateTime.now().offset
        val ldt = date?.atOffset(o)?.toLocalDateTime()
        val fmt = DateTimeFormatter.ofPattern(getPattern(component))
        return ldt?.format(fmt) ?: "error"
    }

    private fun getPattern(component: UIComponent): String {
        val attributes = component?.getAttributes()
        val formatString: String = attributes?.get("pattern") as String ?: "dd.MM.yyyy HH:mm:ss"
        return formatString
    }
}