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
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.montigny.client.API_URL_GET_DEPARTMENT_SECTIONS
import org.montigny.client.API_URL_ORG_DELETE
import org.montigny.client.API_URL_ORG_DEPT_ALL
import org.montigny.client.API_URL_ORG_DEPT_DELETE_BY_ID
import org.montigny.client.API_URL_ORG_DEPT_GET_BY_ID
import org.montigny.client.API_URL_ORG_DEPT_REGISTER
import org.montigny.client.API_URL_ORG_DEPT_UPDATE
import org.montigny.client.API_URL_ORG_GET_ALL
import org.montigny.client.API_URL_ORG_GET_BY_ID
import org.montigny.client.API_URL_ORG_REGISTER
import org.montigny.client.API_URL_ORG_SECT_ALL
import org.montigny.client.API_URL_ORG_SECT_DELETE_BY_ID
import org.montigny.client.API_URL_ORG_SECT_REGISTER
import org.montigny.client.API_URL_ORG_SECT_UPDATE
import org.montigny.client.API_URL_ORG_UPDATE
import org.montigny.client.API_URL_ORG_WITH_NESTED
import org.montigny.client.dto.Common.ErrorResponse
import org.montigny.client.dto.Common.IdResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.dto.Organization.OrganizationResponse
import org.montigny.client.service.OrganizationService
import org.montigny.client.service.UserService
import org.montigny.client.util.Constants.Companion.BEARER_AUTH_TAG
import org.montigny.client.util.Constants.Companion.CREATED_ID_DESCRIPTION
import org.montigny.client.util.Constants.Companion.DELETED_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.ERROR_EXCEPTION
import org.montigny.client.util.Constants.Companion.GET_DPTS_DESCRIPTION_TYPE
import org.montigny.client.util.Constants.Companion.GET_ORGS_DESCRIPTION_TYPE
import org.montigny.client.util.Constants.Companion.GET_SECTS_DESCRIPTION_TYPE
import org.montigny.client.util.Constants.Companion.REGISTER_ORGANIZATION_DESCRIPTION
import org.montigny.client.util.Constants.Companion.UPDATED_SUCCESS_DESCRIPTION
import org.montigny.client.util.Constants.Companion.UPDATE_ORGANIZATION_DESCRIPTION
import org.montigny.client.util.UserUtils
import java.util.*


fun Route.orgRoutes(orgService: OrganizationService, userService: UserService) {

    // ----------------- REGISTER ORGANIZATION -----------------
    route(API_URL_ORG_REGISTER) {
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
    route(API_URL_ORG_UPDATE) {
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
    route(API_URL_ORG_GET_BY_ID) {
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
    route(API_URL_ORG_DELETE) {
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
                    description(DELETED_SUCCESS_DESCRIPTION)
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
    //================== CREATE DEPARTMENT ============
    route(API_URL_ORG_DEPT_REGISTER) {
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
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("${ERROR_EXCEPTION} ${e.message}", 400))
                }
                catch (e:Exception){
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("${ERROR_EXCEPTION} ${e.message}", 500))
                }
            }
            }
        }
    }
    //==================GET DEPARTMENT==================================
    route(API_URL_ORG_DEPT_GET_BY_ID){
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
    //======== UPDATE DEPARTMENT =======================================
    route(API_URL_ORG_DEPT_UPDATE){
        install(NotarizedRoute()){
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            post = PostInfo.builder {
                summary("Update Department")
                description("Update department for the organization")
                request {
                    requestType<CreateDptDTO>()
                    description("Update department for the organization")
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateDptDTO.serializer(),
                                CreateDptDTO(
                                    id = UUID.randomUUID(),
                                    displayName = "updated department name",
                                    organizationId = UUID.randomUUID()
                                )
                            )
                        )
                    )
                    mediaTypes("application/json")
                }
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(UPDATED_SUCCESS_DESCRIPTION)
                }
                authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
                    post{
                        try {
                            val currentUser = UserUtils.getLoggedInUser(call)
                            val req = call.receive<CreateDptDTO>()
                             orgService.updateDept(currentUser, req)
                            call.respond(HttpStatusCode.OK, SimpleResponse(UPDATED_SUCCESS_DESCRIPTION))
                        }
                        catch (e: IllegalArgumentException){
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse("${ERROR_EXCEPTION} ${e.message}", 400))
                        }
                        catch (e:Exception){
                            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("${ERROR_EXCEPTION} ${e.message}", 500))
                        }
                    }
                }
            }
        }
    }
    //=============DELETE DEPARTMENT=============================
    route(API_URL_ORG_DEPT_DELETE_BY_ID){
        install(NotarizedRoute()){
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING))
            delete = DeleteInfo.builder {
                summary("Delete Department")
                description("Deletes a department if there are no dependent sections.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(DELETED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            delete {
                try {
                    val dptId = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

                    val currentUser = UserUtils.getLoggedInUser(call)
                    orgService.deleteDeptById(currentUser,dptId)
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
    //=============CREATE SECTION================================
    route(API_URL_ORG_SECT_REGISTER){
        install(NotarizedRoute()){
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            post = PostInfo.builder {
                summary("Register Section")
                description("Register a section for the organization")
                request {
                    requestType<CreateSectDTO>()
                    description("Register a section for the organization")
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateSectDTO.serializer(),
                                CreateSectDTO(
                                    id = UUID.randomUUID(),
                                    displayName = "section name",
                                    departmentId = UUID.randomUUID()
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
            authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
                post{
                    try {
                        val currentUser = UserUtils.getLoggedInUser(call)
                        val rawBody = call.receiveText()
                        val jsonElement = Json.parseToJsonElement(rawBody).jsonObject

                        val idStr = jsonElement["id"]?.jsonPrimitive?.contentOrNull
                        val displayNameStr = jsonElement["displayName"]?.jsonPrimitive?.content
                        val deptStr = jsonElement["departmentId"]?.jsonPrimitive?.contentOrNull

                        val parsedId = try {
                            idStr?.let { UUID.fromString(it) }
                        } catch (e: Exception) {
                            println("ERROR parsing id: $idStr -> ${e.message}")
                            throw e
                        }
                        val parsedDeptId = try {
                            deptStr?.let { UUID.fromString(it) }
                        } catch (e: Exception) {
                            println("ERROR parsing departmentId: $deptStr -> ${e.message} size ==${deptStr?.length}")
                            throw e
                        }
                        val req = CreateSectDTO(
                            id = parsedId,
                            displayName = displayNameStr ?: "",
                            departmentId = parsedDeptId
                        )
                        val createdId = orgService.createSect(currentUser, req)
                        call.respond(HttpStatusCode.OK, IdResponse(createdId.toString()))
                    }
                    catch (e: IllegalArgumentException){
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("${ERROR_EXCEPTION} ${e.message}", 400))
                    }
                    catch (e:Exception){
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("${ERROR_EXCEPTION} ${e.message}", 500))
                    }
                }
            }
        }
    }

    //================UPDATE SECTION===========================
    route(API_URL_ORG_SECT_UPDATE){
        install(NotarizedRoute()){
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())

            post = PostInfo.builder {
                summary("Update Section")
                description("Update section in organization")
                request {
                    requestType<CreateSectDTO>()
                    description("Update section for the organization")
                    examples(
                        "application/json" to MediaType.Example(
                            value = Json.encodeToJsonElement(
                                CreateSectDTO.serializer(),
                                CreateSectDTO(
                                    id = UUID.randomUUID(),
                                    displayName = "updated section name",
                                    departmentId = UUID.randomUUID()
                                )
                            )
                        )
                    )
                    mediaTypes("application/json")
                }
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(UPDATED_SUCCESS_DESCRIPTION)
                }
            }
            authorize(userService, UserRole.ADMIN,
                UserRole.SUPER_USER) {
                post{
                    try {
                        val currentUser = UserUtils.getLoggedInUser(call)
                        val req = call.receive<CreateSectDTO>()
                        orgService.updateSect(currentUser, req)
                        call.respond(HttpStatusCode.OK, SimpleResponse(UPDATED_SUCCESS_DESCRIPTION))
                    }
                    catch (e: IllegalArgumentException){
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("${ERROR_EXCEPTION} ${e.message}", 400))
                    }
                    catch (e:Exception){
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("${ERROR_EXCEPTION} ${e.message}", 500))
                    }
                }
            }
        }
    }

    //==============DELETE SECTION============================
    route(API_URL_ORG_SECT_DELETE_BY_ID){
        install(NotarizedRoute()){
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            parameters = listOf(
                Parameter(
                    name = "id",
                    `in` = Parameter.Location.path,
                    schema = TypeDefinition.STRING))
            delete = DeleteInfo.builder {
                summary("Delete Section")
                description("Deletes a section.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description(DELETED_SUCCESS_DESCRIPTION)
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {
            delete {
                try {
                    val dptId = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

                    val currentUser = UserUtils.getLoggedInUser(call)
                    orgService.deleteSectById(currentUser,dptId)
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


    //=================GET ALL ORGS =============
    route(API_URL_ORG_GET_ALL){
        install(NotarizedRoute()){
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary(GET_ORGS_DESCRIPTION_TYPE)
                description(GET_ORGS_DESCRIPTION_TYPE)
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<UserTypesDTO>>()
                    description(GET_ORGS_DESCRIPTION_TYPE)
                }
            }
            }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER) {

            get{
                try {
                    val currentUser = UserUtils.getLoggedInUser(call)
                    val orgs = orgService.getAllOrg(currentUser)
                    call.respond(HttpStatusCode.OK,orgs)
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
    //===============GET ALL DEPARTMENTS================
    route(API_URL_ORG_DEPT_ALL){
        install(NotarizedRoute()){
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary(GET_DPTS_DESCRIPTION_TYPE)
                description(GET_DPTS_DESCRIPTION_TYPE)
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<UserTypesDTO>>()
                    description(GET_DPTS_DESCRIPTION_TYPE)
                }
            }
        }
        authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER,UserRole.CAPTURER) {

            get{
                try {
                    val currentUser = UserUtils.getLoggedInUser(call)
                    val depts = orgService.getAllDept(currentUser)
                    call.respond(HttpStatusCode.OK,depts)
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


//=============GET ALL SECTIONS
    route(API_URL_ORG_SECT_ALL){
        install(NotarizedRoute()){
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary(GET_SECTS_DESCRIPTION_TYPE)
                description(GET_SECTS_DESCRIPTION_TYPE)
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<CreateSectDTO>>()
                    description(GET_SECTS_DESCRIPTION_TYPE)
                }
            }
        }
        authorize(userService, UserRole.ADMIN,UserRole.SUPER_USER,UserRole.CAPTURER) {

            get{
                try {
                    val currentUser = UserUtils.getLoggedInUser(call)
                    val orgs = orgService.getAllSect(currentUser)
                    call.respond(HttpStatusCode.OK,orgs)
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
    route(API_URL_GET_DEPARTMENT_SECTIONS){
        install(NotarizedRoute())
        {
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary("Get sections in department")
                description("Get sections in department")
                parameters = listOf(
                    Parameter(
                        name = "id",
                        `in` = Parameter.Location.path,
                        schema = TypeDefinition.STRING))
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<CreateSectDTO>>()
                    description("Returns the sections in a department")
                }
            }
        }
        authorize(userService, UserRole.ADMIN) {
            get {

                try {
                    val currentUser = UserUtils.getLoggedInUser(call)
                    val departmentId = UUID.fromString(call.parameters["id"]!!)
                    val sections = orgService.getSectionByDepartmentId(currentUser,
                        departmentId)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("Sections not found", 404)
                        )
                    call.respond(HttpStatusCode.OK, sections)
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
    // ----------------- GET ORGANIZATION WITH NESTED STRUCTURE -----------------
    route(API_URL_ORG_WITH_NESTED) {
        install(NotarizedRoute()) {
            tags = setOf("Organization")
            security = mapOf(BEARER_AUTH_TAG to emptyList())
            get = GetInfo.builder {
                summary("Get Organizations with nested departments and sections")
                description("Retrieves all organizations (and their departments and sections) visible to the current user.")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<List<OrganizationResponse>>()
                    description("Returns list of organizations with nested structure.")
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER){
        get {
            try {
                val currentUserDTO = UserUtils.getLoggedInUser(call)
                val organizations:List<OrganizationResponse> = orgService.getOrganizationWithNestedDepartments(currentUserDTO)
                call.respond(HttpStatusCode.OK, organizations)

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


