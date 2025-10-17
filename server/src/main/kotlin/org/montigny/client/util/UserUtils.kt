package org.montigny.client.util



import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.montigny.client.dto.Common.ErrorResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.persistence.AppUsers
import org.montigny.client.persistence.Departments
import org.montigny.client.persistence.Organizations
import org.montigny.client.persistence.Sections
import org.montigny.client.persistence.UserTypes
import java.util.*

object UserUtils {

    /**
     * Extracts the logged-in userId from JWT principal.
     * Throws an Unauthorized or BadRequest response if missing/invalid.
     */
    suspend fun getCurrentUserIdOrRespond(call: ApplicationCall): UUID {
        val principal = call.principal<JWTPrincipal>()
            ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Missing JWT Principal", 403))
                throw BadRequestException("Unauthorized: Principal missing")
            }

        return principal.payload.getClaim("userId")
            ?.asString()
            ?.let(UUID::fromString)
            ?: run {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid userId claim", 400))
                throw BadRequestException("Invalid userId claim")
            }
    }

    /**
     * Returns the logged-in user's database row (ResultRow).
     * Throws if user is not found.
     */
    fun getCurrentUserRow(userId: UUID): ResultRow =
        transaction {
            AppUsers.select { AppUsers.id eq userId }
                .firstOrNull()
                ?: throw IllegalStateException("Logged-in user not found")
        }



    suspend fun getLoggedInUser(call: ApplicationCall): CurrentUserDTO {
        val principal = call.principal<JWTPrincipal>()
            ?: run {
                call.respond(HttpStatusCode.Unauthorized, "Missing JWT Principal")
                throw BadRequestException("Unauthorized: Principal missing")
            }

        val userId = principal.payload.getClaim("userId")
            ?.asString()
            ?.let(UUID::fromString)
            ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid userId claim in token")
                throw BadRequestException("Invalid userId claim")
            }

        // Perform a single transaction with LEFT JOINs
        val row = transaction {
            (AppUsers leftJoin UserTypes leftJoin Organizations leftJoin Departments leftJoin Sections)
                .select { AppUsers.id eq userId }
                .limit(1)
                .firstOrNull()
        } ?: run {
            call.respond(HttpStatusCode.NotFound, "Logged-in user not found")
            throw NotFoundException("Logged-in user not found")
        }

        return CurrentUserDTO(
            id = row[AppUsers.id].value,
            name = row[AppUsers.name],
            surname = row[AppUsers.surname],
            email = row[AppUsers.email],
            phoneNumber = row[AppUsers.phoneNumber],

            userTypeId = row[AppUsers.userTypeId]?.value,
            userTypeName = row[AppUsers.userTypeId]?.let { row[UserTypes.displayname] },

            organizationId = row[AppUsers.organizationId]?.value,
            organizationName = row[AppUsers.organizationId]?.let { row[Organizations.displayname] },

            departmentId = row[AppUsers.departmentId]?.value,
            departmentName = row[AppUsers.departmentId]?.let { row[Departments.displayname] },

            sectionId = row[AppUsers.sectionId]?.value,
            sectionName = row[AppUsers.sectionId]?.let { row[Sections.displayname] },
        )
    }


    /**
     * Helper: check if a user is SUPER_USER
     */
    fun isSuperUser(userRow: ResultRow): Boolean {
        val superUserTypeId = transaction {
            UserTypes
                .slice(UserTypes.id)
                .select { UserTypes.displayname eq UserRole.SUPER_USER.displayName }
                .map { it[UserTypes.id].value }
                .firstOrNull()
        }
        return (superUserTypeId != null && userRow[AppUsers.userTypeId]?.value == superUserTypeId)
    }

    /**
     * Helper: check if a user is ADMIN
     */
    fun isAdminUser(userRow: ResultRow): Boolean {
        val adminUserTypeId = transaction {
            UserTypes
                .slice(UserTypes.id)
                .select { UserTypes.displayname eq UserRole.ADMIN.displayName }
                .map { it[UserTypes.id].value }
                .firstOrNull()
        }
        return (adminUserTypeId != null && userRow[AppUsers.userTypeId]?.value == adminUserTypeId)
    }


    fun isSuperUser(user: CurrentUserDTO): Boolean {
        return user.userTypeName?.equals(UserRole.SUPER_USER.displayName, ignoreCase = true) == true
    }

    fun isAdminUser(user: CurrentUserDTO): Boolean {
        return user.userTypeName?.equals(UserRole.ADMIN.displayName, ignoreCase = true) == true
    }
    fun  isCapturerUser(user: CurrentUserDTO): Boolean {
        return user.userTypeName?.equals(UserRole.CAPTURER.displayName, ignoreCase = true) == true
    }


    /**
     * Helper: check if a user is SUPER_USER
     */
    fun isSuperUser_(userRow: ResultRow): Boolean {
        val superUserTypeId = transaction {
            UserTypes
                .slice(UserTypes.id)
                .select { UserTypes.displayname eq UserRole.SUPER_USER.displayName }
                .map { it[UserTypes.id].value }
                .firstOrNull()
        }
        return (superUserTypeId != null && userRow[AppUsers.userTypeId]?.value == superUserTypeId)
    }

    /**
     * Helper: check if a user is ADMIN
     */
    fun isAdminUser_(userRow: ResultRow): Boolean {
        val adminUserTypeId = transaction {
            UserTypes
                .slice(UserTypes.id)
                .select { UserTypes.displayname eq UserRole.ADMIN.displayName }
                .map { it[UserTypes.id].value }
                .firstOrNull()
        }
        return (adminUserTypeId != null && userRow[AppUsers.userTypeId]?.value == adminUserTypeId)
    }

}
