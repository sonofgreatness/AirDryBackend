package org.montigny.client.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import org.montigny.client.domain.api.dto.User.UserRegisterRequest
import java.util.Date

class JwtProvider(private val secret: String, private val issuer: String, private val audience: String, private val expirySeconds: Long) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(user: UserRegisterRequest): String {
        val now = System.currentTimeMillis() / 1000
        val expiry = now + expirySeconds
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", user.id.toString())
            .withClaim("userTypeId", user.userTypeId.toString())
            .withExpiresAt(Date(expiry * 1000))
            .sign(algorithm)
    }

    fun configureKtorAuth(config: AuthenticationConfig) {
        config.jwt("auth-jwt") {
            realm = "Access to API"
            verifier(
                JWT.require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val userTypeId = credential.payload.getClaim("userTypeId").asString()


                if (userId != null && userTypeId != null)
                         JWTPrincipal(credential.payload)
                else null
            }

            challenge { _, _ ->
                println("JWT Verification failed (token missing, invalid, or expired)")
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}


