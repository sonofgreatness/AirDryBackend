package org.montigny.client.domain.api.route


import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.metadata.PutInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.MediaType
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.montigny.client.domain.api.dto.Auth.AuthRequest
import org.montigny.client.domain.api.dto.Auth.AuthResponse
import org.montigny.client.domain.api.dto.Common.ErrorResponse
import org.montigny.client.domain.api.dto.Common.IdResponse
import org.montigny.client.domain.api.dto.Common.PaginatedResponse
import org.montigny.client.domain.api.dto.Common.SimpleResponse
import org.montigny.client.domain.api.dto.User.UpdateUserDTO
import org.montigny.client.domain.api.dto.User.UserRegisterRequest
import org.montigny.client.domain.api.dto.User.UserResponse
import org.montigny.client.domain.api.dto.User.UserTypesDTO
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.service.UserService
import org.montigny.client.util.ApiExamples.Companion.exampleLogin
import org.montigny.client.util.ApiExamples.Companion.exampleUdateUser
import org.montigny.client.util.ApiExamples.Companion.exampleUser
import org.montigny.client.util.Constants.Companion.BEARER_AUTH_TAG
import org.montigny.client.util.Constants.Companion.ERROR_EXCEPTION
import org.montigny.client.util.Constants.Companion.GET_USERS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.GET_USERS_DESCRIPTION_TYPE
import org.montigny.client.util.Constants.Companion.UPDATED_USER_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.UPDATE_USER_DESCRIPTION
import org.montigny.client.util.UserUtils
import java.util.*

fun Route.userRoutes(userService: UserService) {
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
                BEARER_AUTH_TAG to emptyList())
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

    //===================UPDATE USER===================
    route("/users/{id}/update"){
        install(NotarizedRoute()) {
            tags = setOf("User")
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING)
            )
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            put = PutInfo.builder {
                summary("Update User")
                description(UPDATE_USER_DESCRIPTION)
                request {
                    requestType<UpdateUserDTO>()
                    description(UPDATE_USER_DESCRIPTION)
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(UpdateUserDTO.serializer(),exampleUdateUser)
                        )
                    )
                    mediaTypes("application/json")
                }
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(UPDATED_USER_SUCCESS_DESCRIPTION)
                }
            }
            authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
                put{
                    try {
                        val id = UUID.fromString(call.parameters["id"]!!)
                        val req = call.receive<UpdateUserDTO>()
                        userService.updateUser(id,req)
                        call.respond(HttpStatusCode.OK, SimpleResponse(UPDATED_USER_SUCCESS_DESCRIPTION))

                    }
                    catch (e: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("$ERROR_EXCEPTION :${e.message}",400))
                    }catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError,ErrorResponse("$ERROR_EXCEPTION :${e.message}",500))
                    }

                }
            }
        }
    }
    //==================GET ALL USERS================
   route("/users/get_all_users") {
       install(NotarizedRoute()) {
           tags = setOf("User")
           security = mapOf(
               BEARER_AUTH_TAG to emptyList())
           parameters = listOf(
               Parameter(
                   name = "page",
                   `in` = Parameter.Location.query,
                   schema = TypeDefinition.INT)
               ,Parameter(
                   name = "pageSize",
                   `in` = Parameter.Location.query,
                   schema = TypeDefinition.INT)
           )
           get = GetInfo.builder {
               summary(GET_USERS_DESCRIPTION)
               description(GET_USERS_DESCRIPTION)
               response {
                   responseCode(HttpStatusCode.OK)
                   responseType<PaginatedResponse<UserResponse>>()
                   description(GET_USERS_DESCRIPTION)
               }
           }
           authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER) {
               get{

                   try {
                       val currentUser = UserUtils.getLoggedInUser(call)
                       val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                       val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

                       val users = userService.getUsers(currentUser,page,pageSize)
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
//===============GET ALL USER TYPES ======
    route("/users/get_all_user_types") {
        install(NotarizedRoute()) {
            tags = setOf("User")
            security = mapOf(
                BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary(GET_USERS_DESCRIPTION_TYPE)
                description(GET_USERS_DESCRIPTION_TYPE)
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<UserTypesDTO>>()
                    description(GET_USERS_DESCRIPTION_TYPE)
                }
            }
            authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER) {
                get{
                    try {
                        val users = userService.getUserTypes()
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
}