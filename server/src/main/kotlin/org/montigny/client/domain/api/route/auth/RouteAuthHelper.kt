package org.montigny.client.domain.api.route.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.montigny.client.service.UserService

fun Route.authorize(
    userService: UserService,
    vararg allowedRoles: UserRole,
    build: Route.() -> Unit
): Route {
    return authenticate("auth-jwt") {

            intercept(ApplicationCallPipeline.Call) {
                val principal = call.principal<JWTPrincipal>()
                println("principal is ${principal.toString()}")
                val userTypeId = principal?.payload?.getClaim("userTypeId")?.asString()

                if (userTypeId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Missing user type")
                    finish()
                } else {

                    val role = userService.resolveUserRole(userTypeId)
                    if (role !in allowedRoles) {
                        call.respond(HttpStatusCode.Forbidden, "You are not allowed to access this resource")
                        finish()
                    }
                }
            }
            build()
        }
    }

enum class UserRole(val displayName: String) {
    ADMIN("admin"),
    CAPTURER("capturer"),
    CASUAL_WORKER("casual_worker"),
    SUPER_USER("super_user");

    companion object {
        fun fromString(value: String): UserRole? =
            entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) }
    }
}