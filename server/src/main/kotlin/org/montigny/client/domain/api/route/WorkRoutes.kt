package org.montigny.client.domain.api.route
import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.MediaType
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.client.request.request
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import io.ktor.server.routing.route
import org.montigny.client.API_URL_WORK_WORK_DELETE_BY_ID
import org.montigny.client.API_URL_WORK_WORK_GET_ALL
import org.montigny.client.API_URL_WORK_WORK_GET_ALL_DAILY_SUMMARY
import org.montigny.client.API_URL_WORK_WORK_GET_BY_ID
import org.montigny.client.API_URL_WORK_WORK_GET_BY_USER_ID
import org.montigny.client.API_URL_WORK_WORK_REGISTER
import org.montigny.client.API_URL_WORK_WORK_UPDATE
import org.montigny.client.PAGE
import org.montigny.client.PAGE_SIZE
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.dto.Common.ErrorResponse
import org.montigny.client.dto.Common.IdResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateWorkDTOStringAsDate
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.service.UserService
import org.montigny.client.service.WorkerService
import org.montigny.client.util.ApiExamples.Companion.exampleCreateWorkDoneDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.Constants.Companion.DELETED_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.GET_USERS_DESCRIPTION_TYPE
import org.montigny.client.util.Constants.Companion.UPDATED_SUCCESS_DESCRIPTION
import org.montigny.client.util.UserUtils
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import java.util.UUID

fun Route.workRoutes(
   userService: UserService,
      workService: WorkerService
) {
    route(API_URL_WORK_WORK_REGISTER) {
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            post = PostInfo.builder {
                summary("Create WorkDone")
                description("Registers a new work object")
                request {
                    requestType<CreateStackCodeDTO>()
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateWorkDoneDTO.serializer(),
                                exampleCreateWorkDoneDTO)
                        )
                    )
                    description("Registers a new work object")
                }
                response {
                    responseCode(HttpStatusCode.Created)
                    responseType<IdResponse>()
                    description("work object created successfully")
                }
            }
        }
        authorize(userService,UserRole.SUPER_USER,UserRole.CAPTURER) {
            post{
                try {
                  val user  = UserUtils.getLoggedInUser(call)
                  val req = call.receive<CreateWorkDoneDTO>()
                  val id = workService.createWork(user,req)
                  call.respond(HttpStatusCode.Created, IdResponse(id.toString()))
                }catch (e:Exception){
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )

                }
            }
        }
    }
    //--------UPDATE WORK_DONE----------------
    route(API_URL_WORK_WORK_UPDATE){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            post = PostInfo.builder {
                summary("Update WorkDone")
                description("Update work object")
                request {
                    requestType<CreateStackCodeDTO>()
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateWorkDoneDTO.serializer(),
                                exampleCreateWorkDoneDTO)
                        )
                    )
                    description("Update work object")
                }
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(UPDATED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService,UserRole.SUPER_USER,UserRole.CAPTURER) {
            post{
                try {
                    val user  = UserUtils.getLoggedInUser(call)
                    val req = call.receive<CreateWorkDoneDTO>()
                    val id = workService.updateWork(user,req)
                    call.respond(HttpStatusCode.OK,
                        SimpleResponse("$UPDATED_SUCCESS_DESCRIPTION  total affected $id"))
                }catch (e:Exception){
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }
    //--------DELETE WORK_DONE-----------------
    route(API_URL_WORK_WORK_DELETE_BY_ID){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            post = PostInfo.builder {
                summary("Delete WorkDone")
                description("Delete work done")
                request {
                    description("Provide the ID of the WorkDone record to delete")
                    requestType<IdResponse>()
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                IdResponse.serializer(),
                                IdResponse(UUID.randomUUID().toString())
                            )
                        )
                    )
                }
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(DELETED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService,UserRole.SUPER_USER,UserRole.CAPTURER) {
            post{
                try {
                    val user = UserUtils.getLoggedInUser(call)
                    val req = call.receive<IdResponse>()
                    val deleted = workService.deleteWorkById(user,req.id.toUUID())
                    call.respond(HttpStatusCode.OK,"${DELETED_SUCCESS_DESCRIPTION} total deleted : $deleted")
                }
                catch (e:Exception){
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
            }
        }
    }
    //---------GET WORK_DONE_BY_ID-----
    route(API_URL_WORK_WORK_GET_BY_ID){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING))
            get = GetInfo.builder {
                summary("Get Work By Id")
                description("Get Work By Id")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<CreateWorkDTOStringAsDate>()
                    description("Get WorkDone by Id")
                }
            }
        }
        authorize(userService,UserRole.SUPER_USER,UserRole.CAPTURER,
            UserRole.CAPTURER,UserRole.CASUAL_WORKER) {
            get {
                try {
                    val user = UserUtils.getLoggedInUser(call)
                    val id = call.parameters["id"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Missing id", 400)
                        )
                    val resp = workService.getWorkById(user, id.toUUID())
                    resp?.let {
                    call.respond(HttpStatusCode.OK, it)
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
    //---------GET WORK_DONE_BY_USER_ID-----
    route(API_URL_WORK_WORK_GET_BY_USER_ID){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING),
            Parameter(name = PAGE, `in` = Parameter.Location.query, schema = TypeDefinition.INT),
            Parameter(name = PAGE_SIZE, `in` = Parameter.Location.query, schema = TypeDefinition.INT)
            )
            get = GetInfo.builder {
                summary("Get Work By User Id")
                description("Get Work By User Id")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<CreateWorkDTOStringAsDate>()
                    description("Get WorkDone by user Id")
                }
            }
        }
        authorize(userService,UserRole.SUPER_USER,UserRole.CAPTURER,
            UserRole.CAPTURER,UserRole.CASUAL_WORKER) {
            get {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE
                    val user = UserUtils.getLoggedInUser(call)
                    val id = call.parameters["id"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Missing id", 400)
                        )
                    val resp = workService.getWorkByUserId(user, id.toUUID(), page = page, pageSize = pageSize)
                    resp?.let {
                        call.respond(HttpStatusCode.OK, it)
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

    //---------GET ALL WORK_DONE-----
    route(API_URL_WORK_WORK_GET_ALL){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            parameters = listOf(
                Parameter(name = PAGE, `in` = Parameter.Location.query, schema = TypeDefinition.INT),
                Parameter(name = PAGE_SIZE, `in` = Parameter.Location.query, schema = TypeDefinition.INT)
            )
            get = GetInfo.builder {
                summary("Get All Work")
                description("Get All Work")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<CreateWorkDTOStringAsDate>>()
                    description("Get All Work")
                }
            }
        }
        authorize(userService,UserRole.SUPER_USER,UserRole.CAPTURER,
            UserRole.CAPTURER,UserRole.CASUAL_WORKER) {
            get {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE
                    val user = UserUtils.getLoggedInUser(call)
                    val resp = workService.getAllWork(user, page = page, pageSize = pageSize)
                    resp?.let {
                        call.respond(HttpStatusCode.OK, it)
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

    route(API_URL_WORK_WORK_GET_ALL_DAILY_SUMMARY){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            parameters = listOf(
                Parameter(name = PAGE, `in` = Parameter.Location.query, schema = TypeDefinition.INT),
                Parameter(name = PAGE_SIZE, `in` = Parameter.Location.query, schema = TypeDefinition.INT)
            )
            get = GetInfo.builder {
                summary("Get All Work Summaries")
                description("Get All Work Summaries")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<WorkDaySummaryDTO>>()
                    description("Get All Work Summaries")
                }
            }
        }
        authorize(userService,UserRole.SUPER_USER,UserRole.CAPTURER,
            UserRole.CAPTURER,UserRole.CASUAL_WORKER, UserRole.ADMIN) {
            get {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: DEFAULT_PAGE_SIZE
                    val user = UserUtils.getLoggedInUser(call)
                    val resp = workService.getDailyWorkSummaries(user, page = page, pageSize = pageSize)
                    resp?.let {
                        call.respond(HttpStatusCode.OK, it)
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

}
