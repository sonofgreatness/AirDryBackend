package org.montigny.client.domain.api.route


import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.metadata.PutInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.MediaType
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.client.request.request
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.montigny.client.domain.api.dto.Auth.AuthRequest
import org.montigny.client.domain.api.dto.Auth.AuthResponse
import org.montigny.client.domain.api.dto.Common.ActivateRequest
import org.montigny.client.domain.api.dto.Common.ErrorResponse
import org.montigny.client.domain.api.dto.Common.IdResponse
import org.montigny.client.domain.api.dto.Common.SimpleResponse
import org.montigny.client.domain.api.dto.User.UserRegisterRequest
import org.montigny.client.domain.api.dto.User.UserResponse
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.service.UserService
import org.montigny.client.util.Constants
import org.montigny.client.util.Constants.Companion.ERROR_EXCEPTION
import org.montigny.client.util.Constants.Companion.ERROR_PRINCIPAL
import org.montigny.client.util.Constants.Companion.GET_USERS_DESCRIPTION
import org.montigny.client.util.UserUtils
import java.util.*

fun Route.userRoutes(userService: UserService) {


    val exampleUser = UserRegisterRequest(
        "Solo",
        "Dube",
        phoneNumber = "76900009",
        secondaryPhoneNumber = "7998909",
        userTypeId = UUID.randomUUID(),
        organizationId = UUID.randomUUID(),
        sectionId = UUID.randomUUID(),
        departmentId = UUID.randomUUID(),
        email = "solo@email.com",
        password = "password",
    )


    val exampleLogin = AuthRequest(
        username = "simphiweiq@gmail.com",
        password = "myverystrongpassword",
    )

    // ----------------- REGISTER -----------------
    route("/users/register") {
        install(NotarizedRoute()) {
            tags = setOf("User")
            post = PostInfo.builder {
                summary("Register User")
                description("Registers a new user in the system")
                request {
                    requestType<UserRegisterRequest>()
                    description("User registration payload")
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(UserRegisterRequest.serializer(), exampleUser)
                        )
                    )
                    mediaTypes("application/json")
                }
                response {
                    responseCode(HttpStatusCode.Created)
                    responseType<IdResponse>()
                    description("Returns the ID of the newly created user")
                }
            }
        }

            post {
                /*  val superUser = UserRegisterRequest(
                "Simphiwe",
                surname = "Magagula",
                phoneNumber = "76911464",
                secondaryPhoneNumber = "79487461",
                userTypeId =UUID.fromString("fdb72c7b-1ef8-4ca1-a488-4d4f33a219ea") ,
                organizationId = null,
                departmentId = null,
                sectionId = null,
                email = "simphiweiq@gmail.com",
                password = "myverystrongpassword"
            )
            val createdUserId = userService.registerUser(superUser)
            call.respond(HttpStatusCode.Created, IdResponse(createdUserId.toString()))
            */

                try {
                    val req = call.receive<UserRegisterRequest>()
                    val id = userService.registerUser(req)
                    call.respond(HttpStatusCode.Created, IdResponse(id.toString()))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Unexpected server error ${e.message}", 500)
                    )
                }
            }
    }

    route("/users/admin_register") {
        install(NotarizedRoute()) {
            tags = setOf("User")
            security = mapOf(
                "BearerAuth" to emptyList())
            post = PostInfo.builder {
                summary("Register Admin User")
                description("Registers a admin new user in the system")
                request {
                    requestType<UserRegisterRequest>()
                    description("User registration payload")
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(UserRegisterRequest.serializer(), exampleUser)
                        )
                    )
                    mediaTypes("application/json")
                }
                response {
                    responseCode(HttpStatusCode.Created)
                    responseType<IdResponse>()
                    description("Returns the ID of the newly created user")
                }
            }
        }
        authorize(userService, UserRole.SUPER_USER) {
            post {
                try {
                    val req = call.receive<UserRegisterRequest>()
                    val id = userService.registerAdmin(req)
                    call.respond(HttpStatusCode.Created, IdResponse(id.toString()))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Unexpected server error ${e.message}", 500)
                    )
                }
            }
        }
    }
    //==================GET ALL USERS================
   route("/users/get_all_users") {
       install(NotarizedRoute()) {
           tags = setOf("User")
           security = mapOf(
               "BearerAuth" to emptyList())
           get = GetInfo.builder {
               summary(GET_USERS_DESCRIPTION)
               description(GET_USERS_DESCRIPTION)
               response {
                   responseCode(HttpStatusCode.OK)
                   responseType<List<UserResponse>>()
                   description(GET_USERS_DESCRIPTION)
               }
           }
           authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER) {
               get{

                   try {

                       val currentUser = UserUtils.getLoggedInUser(call)
                       println("currentUser = ${UserUtils.getLoggedInUser(call)}")
                       val users = userService.getUsers(currentUser)
                       call.respond(HttpStatusCode.OK, users)

                   } catch (e: IllegalStateException) {
                       call.respond(HttpStatusCode.BadRequest, ErrorResponse("$ERROR_EXCEPTION :${e.message}",400))
                   } catch (e: IllegalArgumentException) {
                       call.respond(HttpStatusCode.BadRequest, ErrorResponse("$ERROR_EXCEPTION :${e.message}",400))
                   } catch (e: Exception) {
                       e.printStackTrace()
                       call.respond(HttpStatusCode.InternalServerError,ErrorResponse("$ERROR_EXCEPTION :${e.message}",500))
                   }

               }
           }
       }
   }


    // ----------------- LOGIN -----------------
    route("/users/login") {
        install(NotarizedRoute()) {
            tags = setOf("User")
            post = PostInfo.builder {
                summary("Login User")
                description("Logs a user in and returns JWT token")
                request {
                    requestType<AuthRequest>()
                    description("Username and password for login")

                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(AuthRequest.serializer(), exampleLogin)
                        )
                    )
                }

                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<AuthResponse>()
                    description("JWT token for authentication")
                }
            }
        }

        post {
            try {
                val req = call.receive<AuthRequest>()
                val token = userService.login(req.username, req.password)
                call.respond(HttpStatusCode.OK, AuthResponse(token))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials", 401))
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.Forbidden, ErrorResponse(e.message ?: "User not active or approved", 403))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected server error", 500))
            }
        }
    }

    // ----------------- APPROVE -----------------
    route("/users/{id}/approve") {
        install(NotarizedRoute()) {
            tags = setOf("User")
            security = mapOf(
                "BearerAuth" to emptyList())
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING)
            )

            put = PutInfo.builder {
                summary("Approve User")
                description("Approve a registered user (admin only)")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description("User approved successfully")
                }
            }
        }

        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
        put {
            val id = UUID.fromString(call.parameters["id"]!!)
            userService.approveUser(id)
            call.respond(HttpStatusCode.OK, SimpleResponse("User approved successfully"))
        }
        }
    }

}