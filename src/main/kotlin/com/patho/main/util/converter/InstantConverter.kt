package com.patho.main.util.converter

import java.time.*
import java.time.format.DateTimeFormatter
import javax.faces.component.UIComponent
import javax.faces.context.FacesContext
import javax.faces.convert.FacesConverter

/**
 * Converter for primefaces calendar to instant
 */
@FacesConverter("instantConverter")
class InstantConverter : CalendarConverter<Instant>() {

    override fun getAsObject(context: FacesContext, component: UIComponent, value: String?): Instant {
        val formatter = DateTimeFormatter.ofPattern(extractPattern(component, context))
        val temporalAccessor = formatter.parse(value)
        val localDateTime = LocalDateTime.from(temporalAccessor)
        val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
        val result = Instant.from(zonedDateTime)
        return result;
    }

    override fun getAsString(context: FacesContext, component: UIComponent, date: Instant): String {
        val o = OffsetDateTime.now().offset
        val ldt = date.atOffset(o).toLocalDateTime()
        val fmt = DateTimeFormatter.ofPattern(extractPattern(component, context))
        return ldt.format(fmt)
    }
}