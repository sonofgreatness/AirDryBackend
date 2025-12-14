package org.montigny.client.auth

import com.russhwolf.settings.Settings

class SessionExpiryRepository(private val settings: Settings) {
    companion object {
        private const val LAST_LOGIN_TIMESTAMP = "last_login_timestamp"
    }

    fun getLastLoginTimestamp(): Long = settings.getLong(LAST_LOGIN_TIMESTAMP, 0L)

    fun setLastLoginTimestamp(timestamp: Long) {
        settings.putLong(LAST_LOGIN_TIMESTAMP, timestamp)
    }
    fun clearLastLoginTimestamp() {
        settings.remove(LAST_LOGIN_TIMESTAMP)
    }
}
