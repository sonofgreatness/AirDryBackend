package org.montigny.client.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OfflineSessionManager {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn

    var userId: String? = null
    private set

    fun login(userId: String) {
        this.userId = userId
        _isLoggedIn.value = true
    }

    fun logout() {
        this.userId = null
        _isLoggedIn.value = false
    }
}