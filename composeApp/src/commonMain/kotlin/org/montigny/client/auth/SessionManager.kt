package org.montigny.client.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.slf4j.Logger
import java.util.*

class SessionManager(
    private val settings: Settings,
    private val logger: Logger
) {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn

    var jwtToken: String?
        get() = settings.getStringOrNull("jwt_token")
        set(value) {
            if (value.isNullOrBlank()) {
                settings.remove("jwt_token")
                settings.remove("user_id")
                settings.remove("user_type_id")
                _isLoggedIn.value = false
            } else {
                settings.putString("jwt_token", value)
                _isLoggedIn.value = decodeJwtToken(value) // <-- safe now
            }
        }

    val userId: String?
        get() = settings.getStringOrNull("user_id")
    val userTypeId: String?
        get() = settings.getStringOrNull("user_type_id")

    fun logout() {
        settings.remove("jwt_token")
        settings.remove("user_id")
        settings.remove("user_type_id")
        _isLoggedIn.value = false
    }

    private fun decodeJwtToken(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return try {
            val decoded = JWT.decode(token)
            val expiry = decoded.expiresAt ?: return false

            // Save claims persistently
            decoded.getClaim("userId").asString()?.let { settings.putString("user_id", it) }
            decoded.getClaim("userTypeId").asString()?.let { settings.putString("user_type_id", it) }

            expiry.after(Date())
        } catch (e: JWTDecodeException) {
            logger.info("JWTDecodeException: ${e.message}")
            false
        }
    }

    fun setUserIds(userId: String, userTypeId: String? = null) {
        settings.putString("user_id", userId)
        userTypeId?.let { settings.putString("user_type_id", it) }
    }

    init {
        // initialize _isLoggedIn from stored token safely
        _isLoggedIn.value = decodeJwtToken(settings.getStringOrNull("jwt_token"))
    }
}


