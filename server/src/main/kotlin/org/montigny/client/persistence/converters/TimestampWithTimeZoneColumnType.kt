package org.montigny.client.persistence.converters

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.sql.Timestamp
import java.time.Instant

class TimestampWithTimeZoneColumnType : ColumnType() {
    override fun sqlType(): String = "TIMESTAMPTZ"

    override fun valueFromDB(value: Any): Any = when (value) {
        is Timestamp -> value.toInstant()
        is java.time.LocalDateTime -> value.toInstant(java.time.ZoneOffset.UTC)
        is Instant -> value
        else -> error("Unexpected value of type ${value::class.qualifiedName} for TIMESTAMPTZ column")
    }

    override fun notNullValueToDB(value: Any): Any = when (value) {
        is Instant -> Timestamp.from(value)
        else -> value
    }

    override fun nonNullValueToString(value: Any): String = "'${notNullValueToDB(value)}'"
}

fun Table.timestamptz(name: String): Column<Instant> =
    registerColumn(name, TimestampWithTimeZoneColumnType())
