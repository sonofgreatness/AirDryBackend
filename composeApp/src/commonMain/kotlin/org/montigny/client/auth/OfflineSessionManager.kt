package org.montigny.client.auth

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OfflineSessionManager(private val settings: Settings) {
    private val _isLoggedIn = MutableStateFlow(settings.getBoolean("offline_logged_in", false))
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn

    var userId: String?
        get() = settings.getStringOrNull("offline_user_id")
        private set(value) {
            if (value == null) {
                settings.remove("offline_user_id")
            } else {
                settings.putString("offline_user_id", value)
            }
        }

    fun login(userId: String) {
        this.userId = userId
        settings.putBoolean("offline_logged_in", true)
        _isLoggedIn.value = true
    }

    fun logout() {
        this.userId = null
        settings.putBoolean("offline_logged_in", false)
        _isLoggedIn.value = false
    }
}
