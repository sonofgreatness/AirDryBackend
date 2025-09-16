package org.montigny.client.domain.api.route


import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.MediaType
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.montigny.client.domain.api.dto.Common.ErrorResponse
import org.montigny.client.domain.api.dto.Common.IdResponse
import org.montigny.client.domain.api.dto.Common.SimpleResponse
import org.montigny.client.domain.api.dto.Organization.CreateDptDTO
import org.montigny.client.domain.api.dto.Organization.CreateOrgDTO
import org.montigny.client.domain.api.dto.Organization.GetOrgDTO
import org.montigny.client.domain.api.dto.User.UserRegisterRequest
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.service.OrganizationService
import org.montigny.client.service.UserService
import org.montigny.client.util.Constants
import org.montigny.client.util.Constants.Companion.BEARER_AUTH_TAG
import org.montigny.client.util.Constants.Companion.CREATED_ID_DESCRIPTION
import org.montigny.client.util.Constants.Companion.REGISTER_ORGANIZATION_DESCRIPTION
import org.montigny.client.util.Constants.Companion.UPDATED_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.UPDATED_USER_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.UPDATE_ORGANIZATION_DESCRIPTION
import org.montigny.client.util.UserUtils
import java.util.UUID


fun Route.orgRoutes(orgService: OrganizationService, userService: UserService) {

    // ----------------- REGISTER ORGANIZATION -----------------
    route("/org/register") {
        install(NotarizedRoute()) {
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            post = PostInfo.builder {
                summary("Register Organization")
                description(REGISTER_ORGANIZATION_DESCRIPTION)
                request {

                    requestType<CreateOrgDTO>()
                    description(REGISTER_ORGANIZATION_DESCRIPTION)
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateOrgDTO.serializer(),
                                CreateOrgDTO(displayName = "Sample Organization")
                            )
                        )
                    )
                    mediaTypes("multipart/form-data")
                }
                response {
                    responseCode(HttpStatusCode.Created)
                    responseType<IdResponse>()
                    description(CREATED_ID_DESCRIPTION)
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
        post {
            try {
                val multipart = call.receiveMultipart()
                var displayName: String? = null
                var id: UUID = UUID.randomUUID()
                var logoBytes: ByteArray? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "displayName" -> displayName = part.value
                                "id" -> if (part.value.isNotBlank()) id = UUID.fromString(part.value)
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "logo") {
                                logoBytes = part.streamProvider().readBytes()
                            }
                        }
                        else -> Unit
                    }
                    part.dispose()
                }
                if (displayName.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing displayName", 400))
                    return@post
                }
                val createOrgDTO = CreateOrgDTO(id = id, displayName = displayName!!)
                val currentUser = UserUtils.getLoggedInUser(call)
                val createdId = orgService.createOrg(currentUser, createOrgDTO, logoBytes)

                call.respond(HttpStatusCode.Created, IdResponse(createdId.toString()))
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
    // ----------------- UPDATE ORGANIZATION -----------------
    route("/org/update") {
        install(NotarizedRoute()) {
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            post = PostInfo.builder {
                summary("Update Organization")
                description(UPDATE_ORGANIZATION_DESCRIPTION)
                request {
                    requestType<CreateOrgDTO>()
                    description(UPDATE_ORGANIZATION_DESCRIPTION)
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateOrgDTO.serializer(),
                                CreateOrgDTO(
                                    id = UUID.randomUUID(),
                                    displayName = "Updated Organization"
                                )
                            )
                        )
                    )
                    mediaTypes("multipart/form-data")
                }
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(UPDATED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            post {
                try {
                    val multipart = call.receiveMultipart()
                    var displayName: String? = null
                    var id: UUID? = null
                    var logoBytes: ByteArray? = null

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> when (part.name) {
                                "displayName" -> displayName = part.value
                                "id" -> if (part.value.isNotBlank()) id = UUID.fromString(part.value)
                            }
                            is PartData.FileItem -> if (part.name == "logo") {
                                logoBytes = part.streamProvider().readBytes()
                            }
                            else -> Unit
                        }
                        part.dispose()
                    }

                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))
                        return@post
                    }
                    if (displayName.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing displayName", 400))
                        return@post
                    }

                    val updateOrgDTO = CreateOrgDTO(id = id!!, displayName = displayName!!)
                    val currentUser = UserUtils.getLoggedInUser(call)
                    orgService.updateOrg(currentUser, updateOrgDTO, logoBytes)

                    call.respond(HttpStatusCode.OK, SimpleResponse(UPDATED_SUCCESS_DESCRIPTION))
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

    // ----------------- GET ORGANIZATION -----------------
    route("/org/{id}/get") {
        install(NotarizedRoute()) {
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary("Get Organization")
                description("Retrieve an organization by its ID")
                parameters = listOf(
                    Parameter(
                        name = "id",
                        `in` = Parameter.Location.path,
                        schema = TypeDefinition.STRING))
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType< GetOrgDTO>()
                    description("Returns the organization details, including Base64-encoded logo")
                }
            }
        }
        get {
            try {
                val orgId = UUID.fromString(call.parameters["id"]!!)
                val org = orgService.getOrgById(orgId)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Organization not found", 404)
                    )
                call.respond(HttpStatusCode.OK, org)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid UUID", 400))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Unexpected server error: ${e.message}", 500)
                )
            }
        }
    }


    // ----------------- DELETE ORGANIZATION -----------------
    route("/org/delete/{id}") {
        install(NotarizedRoute()) {
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING))
            delete = DeleteInfo.builder {
                summary("Delete Organization")
                description("Deletes an organization if there are no dependent departments or users.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description("Organization deleted successfully.")
                }
            }
        }

        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            delete {
                try {
                    val orgId = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

                    val currentUser = UserUtils.getLoggedInUser(call)
                    orgService.deleteOrgById(currentUser,orgId)
                    call.respond(HttpStatusCode.OK, SimpleResponse("Organization deleted successfully"))
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
    //================== CREATE DEPARTMENT ============
    route("/org/department/register") {
        install(NotarizedRoute()) {
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            post = PostInfo.builder {
                summary("Register Department")
                description("Register a department for the organization")
                request {
                    requestType<CreateDptDTO>()
                    description("Register a department for the organization")
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateDptDTO.serializer(),
                                CreateDptDTO(
                                    id = UUID.randomUUID(),
                                    displayName = "department name",
                                    organizationId = UUID.randomUUID()
                                )
                            )
                        )
                    )
                    mediaTypes("application/json")
                }
                response {
                    responseCode(HttpStatusCode.Created)
                    responseType<IdResponse>()
                    description(CREATED_ID_DESCRIPTION)
                }
            }
            authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER) {
            post{
                try {
                    val currentUser = UserUtils.getLoggedInUser(call)
                    val req = call.receive<CreateDptDTO>()
                    val createdId = orgService.createDept(currentUser, req)
                    call.respond(HttpStatusCode.OK, IdResponse(createdId.toString()))
                }
                catch (e: IllegalArgumentException){
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(Constants.ERROR_EXCEPTION, 400))
                }
                catch (e:Exception){
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("${Constants.ERROR_EXCEPTION} ${e.message}", 500))
                }
            }
            }
        }
    }
    //==================GET DEPARTMENT==================================
    route("/org/department/{id}/get"){
        install(NotarizedRoute()) {
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary("Get Department")
                description("Retrieve an department by its ID")
                parameters = listOf(
                    Parameter(
                        name = "id",
                        `in` = Parameter.Location.path,
                        schema = TypeDefinition.STRING))
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<CreateDptDTO>()
                    description("Returns the department details")
                }
            }
            authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER) {
                get {
                    try {
                        val dptId = UUID.fromString(call.parameters["id"]!!)
                        val dpt = orgService.getDeptById(dptId)
                            ?: return@get call.respond(
                                HttpStatusCode.NotFound,
                                ErrorResponse("Department not found", 404)
                            )
                        call.respond(HttpStatusCode.OK, dpt)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid UUID", 400))
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("Unexpected server error: ${e.message}", 500)
                        )
                    }
                }
            }
            }
        }


}


