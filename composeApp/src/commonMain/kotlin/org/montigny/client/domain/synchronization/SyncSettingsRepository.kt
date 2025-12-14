package org.montigny.client.domain.synchronization

import com.russhwolf.settings.Settings

class SyncSettingsRepository(private val settings: Settings) {
    companion object {
        private const val SYNC_INTERVAL_HOURS = "sync_interval_hours"
        private const val LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
    }

    fun getSyncIntervalHours(): Long = settings.getLong(SYNC_INTERVAL_HOURS, 24)
    fun setSyncIntervalHours(hours: Long) = settings.putLong(SYNC_INTERVAL_HOURS, hours)

    fun getLastSyncTimestamp(): Long = settings.getLong(LAST_SYNC_TIMESTAMP, 0L)
    fun setLastSyncTimestamp(timestamp: Long) = settings.putLong(LAST_SYNC_TIMESTAMP, timestamp)
}