package org.montigny.client.auth

import org.montigny.client.data.AppStorage
import java.util.Date
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT

class SessionManager(private val storage: AppStorage) {

    var jwtToken: String?
        get() = storage.get("jwt_token")
        set(value) {
            if (value.isNullOrBlank()) storage.remove("jwt_token")
            else storage.put("jwt_token", value)
        }

    /**
     * Returns true if the stored token exists and is not expired
     */
    val isLoggedIn: Boolean
        get() = decodeJwtToken(jwtToken)

    /**
     * Returns userId from JWT if logged in, null otherwise
     */
    val userId: String?
        get() = extractClaim("userId")

    /**
     * Returns userTypeId from JWT if logged in, null otherwise
     */
    val userTypeId: String?
        get() = extractClaim("userTypeId")

    fun logout() {
        storage.remove("jwt_token")
    }

    /**
     * Decodes stored token to see if valid and not expired
     */
    private fun decodeJwtToken(token: String?): Boolean {
        if (token.isNullOrBlank()) return false

        return try {
            val decoded = JWT.decode(token)
            val expiry = decoded.expiresAt ?: return false
            expiry.after(Date())
        } catch (e: JWTDecodeException) {
            false
        }
    }

    /**
     * Safely extract a claim from JWT, null if invalid
     */
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