package org.montigny.client.data.api.implementation

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.logging.Logger
import org.montigny.client.API_URL_USERS_UNPAGED
import org.montigny.client.API_URL_USERTYPES_GET_ALL
import org.montigny.client.API_URL_USER_CURRENT
import org.montigny.client.API_URL_USER_GET_ALL
import org.montigny.client.API_URL_USER_LOGIN
import org.montigny.client.API_URL_USER_REGISTER
import org.montigny.client.API_USER_GET_BY_ID
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.dto.Auth.AuthRequest
import org.montigny.client.dto.Auth.AuthResponse
import org.montigny.client.dto.Common.IdResponse
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.UUID

class UserApiImpl(private val client: HttpClient, private val sessionManager: SessionManager,
                  private val logger: Logger): UserApi {

    override suspend fun login(username: String, password: String): Boolean {
        val response: AuthResponse = client.post("http://localhost:9090$API_URL_USER_LOGIN") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(username, password))
        }.body()
        sessionManager.jwtToken= response.token
        return true
    }
   override fun logout() {
        sessionManager.logout()
    }
    override suspend fun getUserTypes() :List<UserTypesDTO>{
        logger.info("User types token is ${sessionManager.jwtToken}")
        val response: List<UserTypesDTO> = client.get("http://localhost:9090$API_URL_USERTYPES_GET_ALL") {
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
    override suspend  fun findById(id: UUID): UserRegisterRequest? {
        val response: UserRegisterRequest = client.get("http://localhost:9090$API_USER_GET_BY_ID") {
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
    override suspend fun getUsers(page: Int, pageSize: Int) :PaginatedResponse<UserResponse>{
        val response : PaginatedResponse<UserResponse> = client.get("http://localhost:9090$API_URL_USER_GET_ALL"){
            contentType(ContentType.Application.Json)
        }.body()
        return response

    }
    override suspend fun createUser(user: UserRegisterRequest): ApiResponse {
        return try {
            val response: IdResponse = client.post("http://localhost:9090$API_URL_USER_REGISTER") {
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
        val response :List<UserRegisterRequest> = client.get("http://localhost:9090$API_URL_USERS_UNPAGED"){
            contentType(ContentType.Application.Json)
        }.body()
        return response

    }

}