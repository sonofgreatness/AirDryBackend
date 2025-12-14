package org.montigny.client.data.api.implementation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.util.logging.*
import org.montigny.client.*
import org.montigny.client.auth.SessionExpiryRepository
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.dto.Auth.AuthRequest
import org.montigny.client.dto.Auth.AuthResponse
import org.montigny.client.dto.Common.IdResponse
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class UserApiImpl(private val client: HttpClient,
                  private val sessionManager: SessionManager,
                  private val logger: Logger,
    private  val sessionExpiryRepository: SessionExpiryRepository): UserApi {

    @OptIn(ExperimentalTime::class)
    override suspend fun login(username: String, password: String): Boolean {
        val response: AuthResponse = client.post("$API_BASE_URL$API_URL_USER_LOGIN") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(username, password))
        }.body()
        sessionManager.jwtToken= response.token
        sessionExpiryRepository.setLastLoginTimestamp(Clock.System.now().toEpochMilliseconds())
        return true
    }

   override fun logout() {
        sessionManager.logout()
    }
    override suspend fun getUserTypes() :List<UserTypesDTO>{
        val response: List<UserTypesDTO> = client.get("$API_BASE_URL$API_URL_USERTYPES_GET_ALL") {
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
    override suspend  fun findById(id: UUID): UserRegisterRequest? {
        val response: UserRegisterRequest = client.get("$API_BASE_URL/user/$id/get_by_id") {
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
   override suspend fun findUserResponseById(id: UUID): UserResponse?{
       val response: UserResponse = client.get("$API_BASE_URL/user/$id/get_user_response_by_id") {
           contentType(ContentType.Application.Json)
       }.body()
       return response
   }
    override suspend fun getUsers(page: Int, pageSize: Int) :PaginatedResponse<UserResponse>?{
        val response : PaginatedResponse<UserResponse>? = client.get("$API_BASE_URL$API_URL_USER_GET_ALL"){
            url {
                parameters.append(PAGE, page.toString())
                parameters.append(PAGE_SIZE, pageSize.toString())
            }
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
    override suspend fun createUser(user: UserRegisterRequest): ApiResponse {
        return try {
            val response: IdResponse = client.post("$API_BASE_URL$API_URL_USER_REGISTER") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }.body()
            ApiResponse.Created(response.id)
        } catch (e: Exception) {
            logger.error("Failed to create user: ${e.message}")
            ApiResponse.Error(code = 500, message = "Failed to create user: ${e.message}")
        }
    }
    override  suspend fun  getUsersUnPaged() : List<UserRegisterRequest>{
        val response :List<UserRegisterRequest> = client.get("$API_BASE_URL$API_URL_USERS_UNPAGED"){
            contentType(ContentType.Application.Json)
        }.body()
        return response

    }
    override suspend fun  getServerHealth(): SimpleResponse
    {
        val response : SimpleResponse = client.get("$API_BASE_URL$API_HEALTH"){
            contentType(ContentType.Application.Json)
        }.body()

      return response
    }
    override suspend fun updateUser(user: UserRegisterRequest): ApiResponse{
        try {
            val response =client.put("$API_BASE_URL/users/${user.id}/update") {
                contentType(ContentType.Application.Json)
                setBody(user.toUpdateUserDTO())
            }
            return if(response.status == HttpStatusCode.OK)
                ApiResponse.Success
            else
                ApiResponse.Error(code = response.status.value, message = "Failed to update user: ${response.bodyAsText()}")
        }catch (e: Exception){
            return ApiResponse.Error(code = 500, message = "Failed to update user: ${e.message}")
        }
    }
    override  suspend fun   deleteUserId(userId: UUID): ApiResponse {
    val response = client.delete("$API_BASE_URL/users/${userId}/delete") {
            contentType(ContentType.Application.Json)
    }
    return if(response.status == HttpStatusCode.OK)
            ApiResponse.Success
        else
            ApiResponse.Error(code = response.status.value, message = "Failed to delete user: ${response.bodyAsText()}")
    }

    override suspend fun getCasualUsers(
        page: Int,
        pageSize: Int,
        queryString: String?
    ): PaginatedResponse<UserResponse>? {
   val resp : PaginatedResponse<UserResponse>?     = client.get("$API_BASE_URL$API_URL_USER_GET_ALL_CASUALS"){
            url {
                parameters.append(QUERY_PAGE, page.toString())
                parameters.append(QUERY_PAGE_SIZE, pageSize.toString())
                queryString?.let { parameters.append(QUERY_STRING, it) }
            }
        }.body()
        return  resp
    }
    override  suspend  fun  getFilteredUsers (page: Int,
        pageSize: Int,filters: WorkDataFiltersDTO
    ) : PaginatedResponse<UserResponse>?{
        try {
            val resp = client.post("$API_BASE_URL$API_URL_USER_GET_FILTERED"){
                contentType(ContentType.Application.Json)
                url {
                    parameters.append(PAGE, page.toString())
                    parameters.append(PAGE_SIZE, pageSize.toString())
                }
                setBody(filters)
            }
            return resp.body()
        }catch (e: Exception){
            e.printStackTrace()
            return null
        }
    }
}