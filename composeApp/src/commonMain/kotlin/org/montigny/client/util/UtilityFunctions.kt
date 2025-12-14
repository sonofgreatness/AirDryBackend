package org.montigny.client.util

import androidx.compose.runtime.remember
import org.montigny.client.HUMAN_READABLE_FORMAT_TEXT
import org.montigny.client.HUMAN_READABLE_FORMAT_TEXT_HOUR_MINUTE
import org.montigny.client.util.Constants.Companion.FILTER_DATE_FORMAT
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
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
            return SimpleDateFormat("EEEE dd-MMM-yyyy").format(Date.from(Instant.now()))
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
            HUMAN_READABLE_FORMAT_TEXT,
            Locale.getDefault())

        fun toHumanReadable(dateString: String): String {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            val outputFormatter = DateTimeFormatter.ofPattern(HUMAN_READABLE_FORMAT_TEXT, Locale.getDefault())

            val date = LocalDate.parse(dateString, inputFormatter)
            return date.format(outputFormatter)
        }

        fun covertNumberTo2DecimalString(number : Float): String {
            return String.format("%.2f m³", number)
        }
        fun covertNumberTo2DecimalString(number : Double): String {
            return String.format("%.2f m³", number)
        }
        fun covertNumberTo3DecimalString(number : Float): String {
            return String.format("%.3f m³", number)
        }
        fun covertNumberTo3DecimalString(number : Double): String {
            return String.format("%.3f m³", number)
        }

        fun extractUUIDFromApiString(loaded: String?): UUID? {
            if (loaded.isNullOrBlank()) return null
            val uuidString = loaded.substringBefore(";").trim()
            return try {
                UUID.fromString(uuidString)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        fun extractFullNameFromApiString(loaded: String?): String {
            if (loaded.isNullOrBlank()) return ""
            return loaded.substringAfter(";", "").trim()
        }
        private val utcZoneId: ZoneId = ZoneOffset.UTC

        private val datePickerFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern(FILTER_DATE_FORMAT, Locale.getDefault())
        fun parseStringFromDatePickerToTimeStamp(dateString: String): Timestamp {
            val localDate = LocalDate.parse(dateString, datePickerFormatter)
            val millis = localDate.atStartOfDay(utcZoneId).toInstant().toEpochMilli()
            return Timestamp(millis)
        }

    }
}