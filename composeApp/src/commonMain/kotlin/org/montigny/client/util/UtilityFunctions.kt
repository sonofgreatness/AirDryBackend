package org.montigny.client.util

import org.montigny.client.HUMAN_READABLE_FORMAT_TEXT_HOUR_MINUTE
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

class UtilityFunctions {


    companion object {
        fun String?.toUUIDOrNull(): UUID? = try {
            this?.let { UUID.fromString(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
        fun UUID?.toStringOrNull():String? {
            if(this == null)
                return null
            return this.toString()
        }
        fun UUID?.toStringCustom():String {
            if(this == null)
                return "null"
            return this.toString()
        }
        fun String.toUUID(): UUID {
            return UUID.fromString(this)
        }

        fun convertBoolToLong(value: Boolean) = if (value) 1L else 0L

        @Throws(IllegalArgumentException::class)
        fun convertLongToBool(value: Long) : Boolean{
            if (value == 1L)
                return true
            if(value ==0L)
                return false
            throw IllegalArgumentException("expecting 0  or 1 but got $value")
        }
        fun generateDayDateForNowSpaced(): String{
            return SimpleDateFormat("EEEE dd-MM-yyyy").format(Date.from(Instant.now()))
        }
        fun formatReadableDate(isoString: String?): String {
            return try {
                if (isoString.isNullOrBlank()) return "-"
                val instant = Instant.parse(isoString)
                val formatter = DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy")
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault())
                formatter.format(instant)
            } catch (e: Exception) {
                e.printStackTrace()
                "-"
            }
        }
        val CustomApiFormatter = DateTimeFormatter.ofPattern(
            "EEEE dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )
        val customApiFormattterHTextHour  = DateTimeFormatter.ofPattern(
            HUMAN_READABLE_FORMAT_TEXT_HOUR_MINUTE,
            Locale.getDefault()
        )
        val customApiFormattterHText  = DateTimeFormatter.ofPattern(
            HUMAN_READABLE_FORMAT_TEXT_HOUR_MINUTE,
            Locale.getDefault()

        )
    }
}