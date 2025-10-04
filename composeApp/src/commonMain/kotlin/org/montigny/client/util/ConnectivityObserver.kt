package org.montigny.client.util


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.montigny.client.SERVER_OK_TEXT
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.util.Constants.Companion.CONNECTIVITY_DELAY_TIME
import org.slf4j.Logger

class ConnectivityObserver(
    private  val userApi: UserApi,
    private val logger: Logger,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isServerReachable = MutableStateFlow(false)
    val isServerReachable: StateFlow<Boolean> = _isServerReachable

    init {
        // start observing immediately
        scope.launch { observeConnectivity() }
    }

    private suspend fun observeConnectivity() {
        while (true) {
            try {
                val response: SimpleResponse = userApi.getServerHealth()
                if (response.message == SERVER_OK_TEXT) {
                    if (!_isServerReachable.value) {
                        logger.info("✅ Server reachable")
                    }
                    _isServerReachable.value = true
                }
            } catch (e: Exception) {
                if (_isServerReachable.value) {
                    logger.warn("❌ Lost connection to server: ${e.message}")
                }
                _isServerReachable.value = false
            }
            delay(CONNECTIVITY_DELAY_TIME) // recheck every 30 seconds (configurable)
        }
    }
}
