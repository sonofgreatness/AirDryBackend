package org.montigny.client.auth

import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Periodically checks whether the user’s session has exceeded 7 days since login.
 * If so, it logs out the user and clears stored credentials.
 */
class SessionExpiryWorker(
    private val sessionManager: SessionManager,
    private val offlineSessionManager: OfflineSessionManager,
    private val sessionExpiryRepository: SessionExpiryRepository,
) {
    companion object {
        private const val CHECK_INTERVAL_HOURS = 6L // check every 6 hours
        private val SESSION_EXPIRY_DURATION_MS = 7.days.inWholeMilliseconds
    }

    @OptIn(ExperimentalTime::class)
    suspend fun startSessionExpiryMonitor() {
        while (true) {
            println("Starting session expiry monitor")
            val now = Clock.System.now().toEpochMilliseconds()
            val lastLogin = sessionExpiryRepository.getLastLoginTimestamp()
            val diff = now - lastLogin

            println("Starting session expiry monitor timestamp $lastLogin")
            if (lastLogin > 0 && diff >= SESSION_EXPIRY_DURATION_MS) {
                println("🕒 Session expired after ${diff / (1000 * 60 * 60 * 24)} days. Logging out...")
                // Logout both online & offline users
                sessionManager.logout()
                offlineSessionManager.logout()
                // Reset last login
                sessionExpiryRepository.clearLastLoginTimestamp()
            }
            // Wait 6 hours before next check
            delay(CHECK_INTERVAL_HOURS * 60 * 60 * 1000L)
        }
    }
}