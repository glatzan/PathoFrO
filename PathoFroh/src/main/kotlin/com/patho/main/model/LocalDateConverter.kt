package com.patho.main.model

import javax.persistence.AttributeConverter
import java.sql.Timestamp
import java.time.LocalDate

class LocalDateConverter : AttributeConverter<LocalDate, Timestamp> {

    override fun convertToDatabaseColumn(attribute: LocalDate?): Timestamp? {
        return if (attribute != null) Timestamp.valueOf(attribute.atStartOfDay()) else null
    }

    override fun convertToEntityAttribute(dbData: Timestamp?): LocalDate? {
        return dbData?.toLocalDateTime()?.toLocalDate()
    }

}