package org.montigny.client.domain.api.route


import io.bkbn.kompendium.core.metadata.DeleteInfo
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
import org.montigny.client.API_HEALTH
import org.montigny.client.API_URL_ADMIN_REGISTER
import org.montigny.client.API_URL_USERS_UNPAGED
import org.montigny.client.API_URL_USERTYPES_GET_ALL
import org.montigny.client.API_URL_USER_CURRENT
import org.montigny.client.API_URL_USER_DELETE
import org.montigny.client.API_URL_USER_GET_ALL
import org.montigny.client.API_URL_USER_GET_ALL_CASUALS
import org.montigny.client.API_URL_USER_LOGIN
import org.montigny.client.API_URL_USER_REGISTER
import org.montigny.client.API_URL_USER_UPDATE
import org.montigny.client.API_USER_GET_BY_ID
import org.montigny.client.API_USER_RESPONSE_GET_BY_ID
import org.montigny.client.API_USER_SEARCH
import org.montigny.client.SERVER_OK_TEXT
import org.montigny.client.dto.Auth.AuthRequest
import org.montigny.client.dto.Auth.AuthResponse
import org.montigny.client.dto.Common.ErrorResponse
import org.montigny.client.dto.Common.IdResponse
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.User.UpdateUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.service.UserService
import org.montigny.client.util.ApiExamples.Companion.exampleLogin
import org.montigny.client.util.ApiExamples.Companion.exampleUdateUser
import org.montigny.client.util.ApiExamples.Companion.exampleUser
import org.montigny.client.util.Constants
import org.montigny.client.util.Constants.Companion.BEARER_AUTH_TAG
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.Constants.Companion.DELETED_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.ERROR_EXCEPTION
import org.montigny.client.util.Constants.Companion.GET_CASUAL_USERS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.GET_USERS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.GET_USERS_DESCRIPTION_TYPE
import org.montigny.client.util.Constants.Companion.UPDATED_USER_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.UPDATE_USER_DESCRIPTION

import org.montigny.client.util.UserUtils
import java.util.*

fun Route.userRoutes(userService: UserService) {
    // ----------------- REGISTER -----------------
    route(API_URL_USER_REGISTER) {
        install(NotarizedRoute()) {
            tags = setOf("User")
            security = mapOf(
                BEARER_AUTH_TAG to emptyList())
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
           authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER){
            post {
                try {
                    val currentUser = UserUtils.getLoggedInUser(call)
                    val req = call.receive<UserRegisterRequest>()
                    val id = userService.registerUser(currentUser,req)
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

    route(API_URL_ADMIN_REGISTER) {
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
    route(API_URL_USER_UPDATE){
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
   route(API_URL_USER_GET_ALL) {
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



    //==================GET ALL CASUAL USERS================
    route(API_URL_USER_GET_ALL_CASUALS) {
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
                    schema = TypeDefinition.INT),
                Parameter(
                    name = "queryString",
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.STRING)
            )
            get = GetInfo.builder {
                summary(GET_CASUAL_USERS_DESCRIPTION)
                description(GET_CASUAL_USERS_DESCRIPTION)
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<PaginatedResponse<UserResponse>>()
                    description(GET_CASUAL_USERS_DESCRIPTION)
                }
            }
            authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER, UserRole.CAPTURER) {
                get{

                    try {
                        val currentUser = UserUtils.getLoggedInUser(call)
                        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                        val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                        val  queryString  = call.request.queryParameters["queryString"]
                        val users = userService.getCasualUsers(currentUser,page,pageSize, queryString)
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
    route(API_URL_USERTYPES_GET_ALL) {
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
    route(API_URL_USER_LOGIN) {
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
    route(API_URL_USER_DELETE){
        install(NotarizedRoute()){
            tags = setOf("User")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING))
            delete = DeleteInfo.builder {
                summary("Delete User")
                description("Deletes a user.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(DELETED_SUCCESS_DESCRIPTION)
                }
            }
            authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER) {
                delete{
                    try {
                        val userId = call.parameters["id"]?.let { UUID.fromString(it) }
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

                        userService.deleteUserId(userId)
                        call.respond(HttpStatusCode.OK, SimpleResponse(DELETED_SUCCESS_DESCRIPTION))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("Unexpected server error: ${e.message}", 500)
                        )
                    }
                }
            }
        }
    }
    //============ GET CROSSCURRENT==================

    route(API_URL_USER_CURRENT){
        install(NotarizedRoute()){
            tags = setOf("User")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
        get  = GetInfo.builder {
            summary("Get User")
            description(GET_USERS_DESCRIPTION_TYPE)
            response {
                responseCode(HttpStatusCode.OK)
                responseType<CurrentUserDTO>()
                description("Get User")
            }
        }
            authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER) {
                get{


                    try {
                        val currentUser = UserUtils.getLoggedInUser(call)
                        call.respond(HttpStatusCode.OK, currentUser)
                    }catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials", 401))
                    } catch (e: IllegalStateException) {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse(e.message ?: "User not active or approved", 403))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected server error", 500))
                    }

                }
            }
        }

    }


    //================== SEARCH USERS WITH FILTERS ==================
    route(API_USER_SEARCH) {
        install(NotarizedRoute()) {
            tags = setOf("User")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            parameters = listOf(
                Parameter(name = "page", `in` = Parameter.Location.query, schema = TypeDefinition.INT),
                Parameter(name = "pageSize", `in` = Parameter.Location.query, schema = TypeDefinition.INT),
                Parameter(name = "name", `in` = Parameter.Location.query, schema = TypeDefinition.STRING),
                Parameter(name = "surname", `in` = Parameter.Location.query, schema = TypeDefinition.STRING),
                Parameter(name = "email", `in` = Parameter.Location.query, schema = TypeDefinition.STRING),
                Parameter(name = "phoneNumber", `in` = Parameter.Location.query, schema = TypeDefinition.STRING),
                Parameter(name = Constants.ORGANIZATION_ID, `in` = Parameter.Location.query, schema = TypeDefinition.STRING),
                Parameter(name = Constants.DEPARTMENT_ID, `in` = Parameter.Location.query, schema = TypeDefinition.STRING),
                Parameter(name = Constants.SECTION_ID, `in` = Parameter.Location.query, schema = TypeDefinition.STRING),
                Parameter(name = Constants.USER_TYPE_ID, `in` = Parameter.Location.query, schema = TypeDefinition.STRING),
            )
            get = GetInfo.builder {
                summary("Search Users")
                description("Search users with optional filters and return paginated results")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<PaginatedResponse<UserResponse>>()
                    description("Paginated list of users matching the filters")
                }
            }
        }

        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            get {
                try {

                    val currentUser = UserUtils.getLoggedInUser(call)
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE
                    val name = call.request.queryParameters["name"]
                    val surname = call.request.queryParameters["surname"]
                    val email = call.request.queryParameters["email"]
                    val phoneNumber = call.request.queryParameters["phoneNumber"]
                    val organizationId= call.request.queryParameters[Constants.ORGANIZATION_ID]
                    val departmentId = call.request.queryParameters[Constants.DEPARTMENT_ID]
                    val sectionId = call.request.queryParameters[Constants.SECTION_ID]
                    val userTypeId = call.request.queryParameters[Constants.USER_TYPE_ID]
                    println("parameters ${call.request.queryParameters}")

                    val filters = mutableMapOf<String, String>()
                    name?.takeIf { it.isNotBlank() }?.let { filters["name"] = it }
                    surname?.takeIf { it.isNotBlank() }?.let { filters["surname"] = it }
                    email?.takeIf { it.isNotBlank() }?.let { filters["email"] = it }
                    phoneNumber?.takeIf { it.isNotBlank() }?.let { filters["phoneNumber"]=it}
                    organizationId?.takeIf { it.isNotBlank() }?.let { filters[Constants.ORGANIZATION_ID]=it}
                    departmentId?.takeIf { it.isNotBlank() }?.let { filters[Constants.DEPARTMENT_ID]=it }
                    sectionId?.takeIf { it.isNotBlank() }?.let { filters[Constants.SECTION_ID] = it }
                    userTypeId?.takeIf { it.isNotBlank() }?.let { filters[Constants.USER_TYPE_ID] = it}
                    val users = userService.getUsersFiltered(currentUser, page, pageSize, filters)
                    call.respond(HttpStatusCode.OK, users)

                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("$ERROR_EXCEPTION :${e.message}", 400))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("$ERROR_EXCEPTION :${e.message}", 400))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("$ERROR_EXCEPTION :${e.message}", 500))
                }
            }
        }
    }

    route(API_USER_RESPONSE_GET_BY_ID) {
      install(NotarizedRoute()){
      tags = setOf("User")
      parameters = listOf(Parameter(name = "id",
          `in` = Parameter.Location.path,
          schema = TypeDefinition.STRING))
      security = mapOf(BEARER_AUTH_TAG to emptyList())
      get = GetInfo.builder {
          summary("Get Users")
          description("Get Users")
          response {
              responseCode(HttpStatusCode.OK)
              responseType<UserResponse>()
              description("users  with matching id")
          }
      }
  }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            get {
                try {
                    val idParam = call.parameters["id"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Missing id", 400)
                        )

                    val userId = try {
                        UUID.fromString(idParam)
                    } catch (e: IllegalArgumentException) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid UUID format", 400)
                        )
                    }
                    val user = userService.findUserResponseById(userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("User not found", 404)
                        )

                    call.respond(HttpStatusCode.OK, user)
                } catch (e: IllegalStateException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("$ERROR_EXCEPTION : ${e.message}", 400)
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("$ERROR_EXCEPTION : ${e.message}", 400)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("$ERROR_EXCEPTION : ${e.message}", 500)
                    )
                }
            }
        }
    }


    //=== Get user by id endpoint
    route(API_USER_GET_BY_ID) {
        install(NotarizedRoute()){
            tags = setOf("User")
            parameters = listOf(Parameter(name = "id",
                `in` = Parameter.Location.path,
                schema = TypeDefinition.STRING))
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary("Get Users")
                description("Get Users")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<UserRegisterRequest>()
                    description("users  with matching id")
                }

            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            get {
                try {
                    val idParam = call.parameters["id"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Missing id", 400)
                        )

                    val userId = try {
                        UUID.fromString(idParam)
                    } catch (e: IllegalArgumentException) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid UUID format", 400)
                        )
                    }

                    val user = userService.findById(userId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("User not found", 404)
                        )

                    call.respond(HttpStatusCode.OK, user)
                } catch (e: IllegalStateException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("$ERROR_EXCEPTION : ${e.message}", 400)
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("$ERROR_EXCEPTION : ${e.message}", 400)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("$ERROR_EXCEPTION : ${e.message}", 500)
                    )
                }
            }
        }
    }

    route(API_URL_USERS_UNPAGED){
        install(NotarizedRoute()){
            tags = setOf("User")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get  = GetInfo.builder {
                summary("Get all users")
                description(GET_USERS_DESCRIPTION_TYPE)
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<UserRegisterRequest>>()
                    description("Get all users")
                }
            }
            authorize(userService, UserRole.ADMIN) {
                get{
                    try {
                        val currentUser = UserUtils.getLoggedInUser(call)
                        val users = userService.getUserUnPaginated(currentUser)
                        call.respond(HttpStatusCode.OK, users)
                    }catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials", 401))
                    } catch (e: IllegalStateException) {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse(e.message ?: "User not active or approved", 403))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected server error", 500))
                    }

                }
            }
        }
    }

 route(API_HEALTH){
     install(NotarizedRoute()){
         tags = setOf("Utility")
        get = GetInfo.builder {
            summary("Get health check")
            description("Health check")
            response {
                responseCode(HttpStatusCode.OK)
                responseType<SimpleResponse>()
                description("Ok for good server health")
            }
        }
     }
     get{
         call.respond(HttpStatusCode.OK, SimpleResponse(SERVER_OK_TEXT))
     }
 }
}

