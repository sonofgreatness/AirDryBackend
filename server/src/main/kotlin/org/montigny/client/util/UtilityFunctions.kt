package org.montigny.client.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

class UtilityFunctions
{
    companion object{
        fun String.toUUID(): UUID {
            return UUID.fromString(this)
        }
        fun generateDayDateForNow(): String{
            return SimpleDateFormat("EEEE dd/MM/yyyy").format(Date.from(Instant.now()))
        }

        private val releaseDateFormatter =
            DateTimeFormatter.ofPattern("EEEE dd MMM yyyy", Locale.ENGLISH)
        fun convertDateToString(date: LocalDate): String {
            return date.format(releaseDateFormatter)
        }
    }
}