package org.montigny.client.data.api.implementation

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.logging.Logger
import org.montigny.client.API_URL_USERTYPES_GET_ALL
import org.montigny.client.API_URL_USER_LOGIN
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.dto.Auth.AuthRequest
import org.montigny.client.dto.Auth.AuthResponse
import org.montigny.client.dto.User.UserTypesDTO

class UserApiImpl(private val client: HttpClient, private val sessionManager: SessionManager,private val logger: Logger): UserApi {

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

}