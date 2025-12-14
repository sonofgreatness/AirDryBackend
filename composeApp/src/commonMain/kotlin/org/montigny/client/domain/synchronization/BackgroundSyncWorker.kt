package org.montigny.client.domain.synchronization

import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class BackgroundSyncWorker(
    private val syncManager: SyncManager,
    private val syncSettingsRepository: SyncSettingsRepository,
) {
    @OptIn(ExperimentalTime::class)
    suspend fun startPeriodicSync() {
        while (true) {
            val lastSync = syncSettingsRepository.getLastSyncTimestamp()
            val intervalHours = syncSettingsRepository.getSyncIntervalHours()
            val now = Clock.System.now().toEpochMilliseconds()
            val diffHours = (now - lastSync) / (1000 * 60 * 60)

            if (diffHours >= intervalHours) {
                println("⏳ Sync triggered after $diffHours hours...")
                try {
                    syncManager.synchronizeAllAppData()
                    syncSettingsRepository.setLastSyncTimestamp(now)
                } catch (e: Exception) {
                    println("❌ Sync failed: ${e.localizedMessage}")
                }
            }

            delay(60 * 60 * 1000L) // check every hour
        }
    }
}