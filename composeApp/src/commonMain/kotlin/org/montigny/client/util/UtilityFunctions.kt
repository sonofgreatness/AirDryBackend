package org.montigny.client.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
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
    }
}