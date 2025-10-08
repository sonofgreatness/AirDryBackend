package org.montigny.client.data.api.implementation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.logging.*
import org.montigny.client.API_BASE_URL
import org.montigny.client.API_URL_ORG_DEPT_ALL
import org.montigny.client.API_URL_ORG_REGISTER
import org.montigny.client.API_URL_ORG_SECT_ALL
import org.montigny.client.API_URL_ORG_WITH_NESTED
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.Organization.OrganizationResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.UUID

class OrganizationApiImpl(private val client: HttpClient,
                          private val logger: Logger): OrganizationApi {
    override suspend fun getAllDept(): List<CreateOrgDTO>
    {
        val response: List<CreateOrgDTO> = client.get("$API_BASE_URL$API_URL_ORG_DEPT_ALL"){
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
    override suspend  fun getAllSect(): List<CreateSectDTO> {
        val response: List<CreateSectDTO> = client.get("$API_BASE_URL$API_URL_ORG_SECT_ALL"){
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
    override suspend fun getSectionByDepartmentId(departmentId: UUID): List<CreateSectDTO>?{
        val response: List<CreateSectDTO> = client.get("$API_BASE_URL/org/department/section/${departmentId}/get_department_sections"){
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
    override suspend  fun getOrganizationWithNestedDepartments():List<OrganizationResponse>{
        val response: List<OrganizationResponse>  = client.get("$API_BASE_URL$API_URL_ORG_WITH_NESTED")
        {
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
    override suspend fun createOrg(req: CreateOrgDTO, logo: ByteArray?): ApiResponse {
        return try {
            val resp = client.submitFormWithBinaryData(
                url = "$API_BASE_URL$API_URL_ORG_REGISTER",
                formData = formData {
                    append("displayName", req.displayName)
                    req.id?.let { append("id", it.toString()) }

                    logo?.let {
                        append(
                            key = "logo",
                            value = it,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"logo.png\"")
                            }) } })
            if (resp.status.isSuccess()) {
                ApiResponse.Success
            } else {
                ApiResponse.Error(
                    resp.status.value,
                    "Failed to create org: ${resp.bodyAsText()}"
                )
            }
        } catch (e: Exception) {
            logger.error("createOrg() error: ${e.localizedMessage}", e)
            ApiResponse.Error(
                HttpStatusCode.InternalServerError.value,
                "Failed to create org: ${e.message}"
            )
        }
    }
    override suspend fun getOrgById(orgId: UUID): GetOrgDTO {
        TODO("Not yet implemented")
    }

    override suspend fun updateOrg(req: CreateOrgDTO, logo: ByteArray?): ApiResponse {
        TODO("Not yet implemented")
    }
}