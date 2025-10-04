package org.montigny.client.data.api.repository

import io.ktor.http.cio.Response
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.*

interface UserApi {

    suspend fun login(username: String, password: String): Boolean
    fun logout()

    suspend fun getUserTypes():List<UserTypesDTO>
    suspend fun findById(id: UUID): UserRegisterRequest?
    suspend fun getUsers(page: Int=1,pageSize: Int=10) :PaginatedResponse<UserResponse>
    suspend fun createUser(user: UserRegisterRequest) : ApiResponse
    suspend fun  getUsersUnPaged() : List<UserRegisterRequest>
    suspend fun  getServerHealth(): SimpleResponse


}
