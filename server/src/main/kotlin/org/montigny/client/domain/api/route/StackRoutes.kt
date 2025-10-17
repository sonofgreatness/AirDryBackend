package org.montigny.client.domain.api.route

import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
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
import org.montigny.client.API_URL_WORK_STACK_CODE_DELETE_BY_ID
import org.montigny.client.API_URL_WORK_STACK_CODE_GET_ALL
import org.montigny.client.API_URL_WORK_STACK_CODE_GET_BY_ID
import org.montigny.client.API_URL_WORK_STACK_CODE_REGISTER
import org.montigny.client.API_URL_WORK_STACK_CODE_UPDATE
import org.montigny.client.API_URL_WORK_STACK_DELETE_BY_ID
import org.montigny.client.API_URL_WORK_STACK_GET_ALL
import org.montigny.client.API_URL_WORK_STACK_GET_BY_ID
import org.montigny.client.API_URL_WORK_STACK_REGISTER
import org.montigny.client.API_URL_WORK_STACK_UPDATE
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.dto.Common.ErrorResponse
import org.montigny.client.dto.Common.IdResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.service.StackService
import org.montigny.client.service.UserService
import org.montigny.client.util.ApiExamples.Companion.exampleCreateStackCodeDTO
import org.montigny.client.util.ApiExamples.Companion.exampleCreateStackDTO
import org.montigny.client.util.Constants.Companion.DELETED_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.UPDATED_SUCCESS_DESCRIPTION
import org.montigny.client.util.UserUtils
import java.util.*


fun Route.stackRoutes(stackService: StackService, userService: UserService) {

     // ---------- CREATE STACK CODE ----------
     route(API_URL_WORK_STACK_CODE_REGISTER) {
            install(NotarizedRoute()) {
                tags = setOf("Work")
                security = mapOf("BearerAuth" to emptyList())
                post = PostInfo.builder {
                    summary("Create StackCode")
                    description("Registers a new stack code")
                    request {
                        requestType<CreateStackCodeDTO>()
                        examples(
                            "application/json" to MediaType.Example(
                                value = Json.encodeToJsonElement(
                                    CreateStackCodeDTO.serializer(),
                                    exampleCreateStackCodeDTO)
                            )
                        )
                        description("Registers a new stack code")
                    }
                    response {
                        responseCode(HttpStatusCode.Created)
                        responseType<IdResponse>()
                        description("StackCode created successfully")
                    }
                }
            }
            authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER,UserRole.CAPTURER) {
                post {
                    try {
                        val req = call.receive<CreateStackCodeDTO>()
                        val currentUser = UserUtils.getLoggedInUser(call)
                        val id = stackService.createStackCode(currentUser, req)
                        call.respond(HttpStatusCode.Created, IdResponse(id.toString()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("Server error: ${e.message}", 500)
                        )
                    }
                }
            }
        }
    // -------------------- CREATE STACK --------------------
    route(API_URL_WORK_STACK_REGISTER) {
        install(NotarizedRoute()) {
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            post = PostInfo.builder {
                summary("Create Stack")
                description("Registers a new stack entry for a worker.")
                request {
                    requestType<CreateStackDTO>()
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateStackDTO.serializer(),exampleCreateStackDTO
                            )
                        )
                    )
                    description("Registers a new stack entry for a worker.")
                }
                response {
                    responseCode(HttpStatusCode.Created)
                    responseType<IdResponse>()
                    description("Stack created successfully.")
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER, UserRole.CAPTURER) {
            post {
                try {
                    val req = call.receive<CreateStackDTO>()
                    val currentUser = UserUtils.getLoggedInUser(call)
                    val id = stackService.createStack(currentUser, req)
                    call.respond(HttpStatusCode.Created, IdResponse(id.toString()))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }
    //----------- STACK CODE UPDATE--------------
    route(API_URL_WORK_STACK_CODE_UPDATE){
    install(NotarizedRoute()) {
        tags = setOf("Work")
        security = mapOf("BearerAuth" to emptyList())
        post = PostInfo.builder {
                summary("Update StackCode")
                description("update stack code")
                request {
                    requestType<CreateStackCodeDTO>()
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateStackCodeDTO.serializer(),
                                exampleCreateStackCodeDTO)
                        )
                    )
                    description("update stack code")
                }
                response {
                    responseCode(HttpStatusCode.Created)
                    responseType<SimpleResponse>()
                    description(UPDATED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            post {
                try {
                    val user  = UserUtils.getLoggedInUser(call)
                    val req = call.receive<CreateStackCodeDTO>()
                    stackService.updateStackCode(user, req)
                    call.respond(HttpStatusCode.OK,SimpleResponse(UPDATED_SUCCESS_DESCRIPTION))
                }catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }
    route(API_URL_WORK_STACK_UPDATE){
        install(NotarizedRoute()) {
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            post = PostInfo.builder {
                summary("Update Stack")
                description("update stack")
                request {
                    requestType<CreateStackDTO>()
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateStackDTO.serializer(),
                                exampleCreateStackDTO)
                        )
                    )
                    description("update stack code")
                }
                response {
                    responseCode(HttpStatusCode.Created)
                    responseType<SimpleResponse>()
                    description(UPDATED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            post {
                try {
                    val user  = UserUtils.getLoggedInUser(call)
                    val req = call.receive<CreateStackDTO>()
                    stackService.updateStack(user, req)
                    call.respond(HttpStatusCode.OK,SimpleResponse(UPDATED_SUCCESS_DESCRIPTION))
                }catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }
    route(API_URL_WORK_STACK_DELETE_BY_ID){
        install(NotarizedRoute()){
            tags = setOf("Work")
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING)
            )
            security = mapOf("BearerAuth" to emptyList())
            delete  = DeleteInfo.builder {
                summary("Delete stack ")
                description("Deletes a stack.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(DELETED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
        delete{
            try {
                val id  =call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

                stackService.deleteStackById(id)
                call.respond(HttpStatusCode.OK,SimpleResponse(DELETED_SUCCESS_DESCRIPTION))
            }
            catch (e: Exception){
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Server error: ${e.message}", 500)
                )
            }
            }
        }
    }
    route(API_URL_WORK_STACK_CODE_DELETE_BY_ID){
        install(NotarizedRoute()){
            tags = setOf("Work")
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING)
            )
            security = mapOf("BearerAuth" to emptyList())
            delete  = DeleteInfo.builder {
                summary("Delete stack code")
                description("Deletes a stack code.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(DELETED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            delete{
                try {
                    val id  =call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))
                    stackService.deleteStackCodeById(id)
                    call.respond(HttpStatusCode.OK,SimpleResponse(DELETED_SUCCESS_DESCRIPTION))
                }
                catch (e: Exception){
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }
    route(API_URL_WORK_STACK_CODE_GET_BY_ID){
        install(NotarizedRoute()){
            tags = setOf("Work")
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING))
            security = mapOf("BearerAuth" to emptyList())
            get = GetInfo.builder {
                summary("Get stack code")
                description("Get stack code.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<CreateStackCodeDTO>()
                    description("Get stack code.")
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            get {
                try {
                    val id = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

                    val stackCode = stackService.getStackCodeByID(id)
                     stackCode?.let {stackCode->
                    call.respond(HttpStatusCode.OK, stackCode)
                     }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }
    route(API_URL_WORK_STACK_GET_BY_ID){
        install(NotarizedRoute()){
            tags = setOf("Work")
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING))
            security = mapOf("BearerAuth" to emptyList())
            get = GetInfo.builder {
                summary("Get stack ")
                description("Get stack .")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<GetStackDTO>()
                    description("Get stack .")
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            get {
                try {
                    val id = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

                    val stack = stackService.getStackById(id)
                    stack?.let {stack->
                    call.respond(HttpStatusCode.OK, stack)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }

    route(API_URL_WORK_STACK_GET_ALL){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            get = GetInfo.builder {
                summary("Get all stacks")
                description("Get all stacks.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<GetStackDTO>>()
                    description("Get all stacks.")
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            get{
                try {
                    val stacks  = stackService.getAllStacks()
                    call.respond(HttpStatusCode.OK, stacks)
                }catch (e: Exception){
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }

    route(API_URL_WORK_STACK_CODE_GET_ALL){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            get = GetInfo.builder {
                summary("Get all stack codes")
                description("Get all stack codes.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<CreateStackCodeDTO>>()
                    description("Get all stack codes.")
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            get{
                try {
                    val stackcodes  = stackService.getAllStackCodes()
                    call.respond(HttpStatusCode.OK, stackcodes)
                }catch (e: Exception){
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }
}
