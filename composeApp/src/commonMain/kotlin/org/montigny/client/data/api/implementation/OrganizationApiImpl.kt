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
import org.montigny.client.API_URL_ORG_DEPT_DELETE_BY_ID
import org.montigny.client.API_URL_ORG_DEPT_REGISTER
import org.montigny.client.API_URL_ORG_DEPT_UPDATE
import org.montigny.client.API_URL_ORG_GET_BY_ID
import org.montigny.client.API_URL_ORG_REGISTER
import org.montigny.client.API_URL_ORG_SECT_ALL
import org.montigny.client.API_URL_ORG_SECT_DELETE_BY_ID
import org.montigny.client.API_URL_ORG_SECT_REGISTER
import org.montigny.client.API_URL_ORG_SECT_UPDATE
import org.montigny.client.API_URL_ORG_UPDATE
import org.montigny.client.API_URL_ORG_WITH_NESTED
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.dto.Organization.CreateDptDTO
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
        API_URL_ORG_GET_BY_ID
         val response = client.get("$API_BASE_URL/org/${orgId}/get"){
             contentType(ContentType.Application.Json)
         }.body<GetOrgDTO>()
        return response
    }

    override suspend fun updateOrg(req: CreateOrgDTO, logo: ByteArray?): ApiResponse {
        return try {
            val resp = client.submitFormWithBinaryData(
                url = "$API_BASE_URL$API_URL_ORG_UPDATE",
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
                    "Failed to update org: ${resp.bodyAsText()}"
                )
            }
        } catch (e: Exception) {
            logger.error("updateOrg() error: ${e.localizedMessage}", e)
            ApiResponse.Error(
                HttpStatusCode.InternalServerError.value,
                "Failed to Update org: ${e.message}"
            )
        }
    }
    override    suspend fun  createSect(req: CreateSectDTO): ApiResponse{

        try{
            val response = client.post("$API_BASE_URL$API_URL_ORG_SECT_REGISTER"){
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            return if (response.status.isSuccess())
                ApiResponse.Success
            else
                ApiResponse.Error(HttpStatusCode.InternalServerError.value,
                    "Failed to create section: ${response.bodyAsText()}")
        }catch (e:Exception){

            logger.error("create section error: ${e.localizedMessage}", e)
            return ApiResponse.Error(HttpStatusCode.InternalServerError.value, "Failed to create department: ${e.message}")
        }
    }
   override suspend fun  createDepartment(req: CreateDptDTO) : ApiResponse {
       try{
           val response = client.post("$API_BASE_URL$API_URL_ORG_DEPT_REGISTER"){
               contentType(ContentType.Application.Json)
               setBody(req)
           }
           return if (response.status.isSuccess())
               ApiResponse.Success
           else
               ApiResponse.Error(HttpStatusCode.InternalServerError.value, "Failed to create department: ${response.bodyAsText()}")
       }catch (e:Exception){

           logger.error("create Department error: ${e.localizedMessage}", e)
           return ApiResponse.Error(HttpStatusCode.InternalServerError.value, "Failed to create department: ${e.message}")
       }
   }
  override  suspend  fun  updateDepartment(req:CreateDptDTO): ApiResponse{
      try {

          val response = client.post("$API_BASE_URL$API_URL_ORG_DEPT_UPDATE"){
              contentType(ContentType.Application.Json)
              setBody(req)
          }
          return if (response.status.isSuccess())
              ApiResponse.Success
          else
              ApiResponse.Error(HttpStatusCode.InternalServerError.value, "Failed to update department: ${response.bodyAsText()}")
      }catch (e:Exception){
          logger.error("updateDepartment() error: ${e.localizedMessage}", e)
          return ApiResponse.Error(HttpStatusCode.InternalServerError.value, "Failed to update department: ${e.message}")
      }
  }

   override  suspend fun  createSection(req: CreateSectDTO): ApiResponse{
       try{
           val response = client.post("$API_BASE_URL$API_URL_ORG_SECT_REGISTER"){
               contentType(ContentType.Application.Json)
               setBody(req)
           }
           return if (response.status.isSuccess())
               ApiResponse.Success
           else
               ApiResponse.Error(HttpStatusCode.InternalServerError.value, "Failed to create section: ${response.bodyAsText()}")
       }catch (e:Exception){
           logger.error("create Section error: ${e.localizedMessage}", e)
           return ApiResponse.Error(HttpStatusCode.InternalServerError.value,
               "Failed to create section: ${e.message}")
       }
   }
   override suspend fun  updateSection(req: CreateSectDTO): ApiResponse{
       try{
           val response = client.post("$API_BASE_URL$API_URL_ORG_SECT_UPDATE"){
               contentType(ContentType.Application.Json)
               setBody(req)
           }
           return if (response.status.isSuccess())
               ApiResponse.Success
           else
               ApiResponse.Error(HttpStatusCode.InternalServerError.value,
                   "Failed to update section: ${response.bodyAsText()}")
       }catch (e:Exception){
           logger.error("update section error: ${e.localizedMessage}", e)
           return ApiResponse.Error(HttpStatusCode.InternalServerError.value,
               "Failed to update section: ${e.message}")
       }
   }
  override suspend fun  deleteSection(id : UUID):ApiResponse{
      val resp = client.delete("$API_BASE_URL/org/department/section/${id}/delete"){
          contentType(ContentType.Application.Json)
      }
      return if (resp.status.isSuccess())
          ApiResponse.Success
      else
          ApiResponse.Error(HttpStatusCode.InternalServerError.value,
              "Failed to delete section: ${resp.bodyAsText()}")
  }
  override  suspend fun  deleteDepartment(id : UUID):ApiResponse{
      API_URL_ORG_DEPT_DELETE_BY_ID
      val resp = client.delete("$API_BASE_URL/org/department/delete/${id}"){
          contentType(ContentType.Application.Json)
      }
      return if (resp.status.isSuccess())
          ApiResponse.Success
      else
          ApiResponse.Error(HttpStatusCode.InternalServerError.value,
              "Failed to delete department: ${resp.bodyAsText()}")
  }
}