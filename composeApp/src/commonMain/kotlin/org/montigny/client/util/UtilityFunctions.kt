package org.montigny.client.util

import java.util.UUID

class UtilityFunctions {


    companion object {
        fun String?.toUUIDOrNull(): UUID? =
            this?.let {
                runCatching { UUID.fromString(it) }.getOrNull()
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

    }
}