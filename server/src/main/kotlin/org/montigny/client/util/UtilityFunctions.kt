package org.montigny.client.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
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
    }
}