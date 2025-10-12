package org.montigny.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionExpiryWorker
import org.montigny.client.domain.synchronization.BackgroundSyncWorker

class AppWorkerManager(
    private val backgroundSyncWorker: BackgroundSyncWorker,
    private val sessionExpiryWorker: SessionExpiryWorker,

) {
    private val appScope = CoroutineScope(Dispatchers.Default)

    fun startAllWorkers() {
        appScope.launch {
            backgroundSyncWorker.startPeriodicSync()
        }
        appScope.launch {
            sessionExpiryWorker.startSessionExpiryMonitor()
        }
    }
}