package org.montigny.client.auth

import org.montigny.client.data.AppStorage
import java.util.Date
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject
import org.slf4j.Logger

class SessionManager(private val storage: AppStorage,
    private val logger: Logger,) {


    private val _isLoggedIn = MutableStateFlow(decodeJwtToken(storage.get("jwt_token")))
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn


    var jwtToken: String?
        get() = storage.get("jwt_token")
        set(value) {
            if (value.isNullOrBlank()) {
                storage.remove("jwt_token")
            } else {
                storage.put("jwt_token", value)
            }
            _isLoggedIn.value = decodeJwtToken(value)
        }

    fun logout() {
        jwtToken = null // will automatically update _isLoggedIn
    }

    private fun decodeJwtToken(token: String?): Boolean {
        if (token.isNullOrBlank())
            return false
        return try {
            val decoded = JWT.decode(token)
            val expiry = decoded.expiresAt ?: return false
            expiry.after(Date())
        } catch (e: JWTDecodeException) {
            logger.info("JWTDecodeException ${e.message}")
            false
        }
    }

    val userId: String?
        get() = extractClaim("userId")
    val userTypeId: String?
        get() = extractClaim("userTypeId")

    private fun extractClaim(claimName: String): String? {
        val token = jwtToken ?: return null
        return try {
            val decoded = JWT.decode(token)
            decoded.getClaim(claimName).asString()
        } catch (e: JWTDecodeException) {
            null
        }
    }
}
