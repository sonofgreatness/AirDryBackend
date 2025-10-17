package org.montigny.client.util

import java.util.UUID

class UtilityFunctions
{
    companion object{
        fun String.toUUID(): UUID {
            return UUID.fromString(this)
        }
    }
}