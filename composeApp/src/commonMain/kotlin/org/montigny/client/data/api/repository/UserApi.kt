package org.montigny.client.data.api.repository

import io.ktor.http.cio.Response
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.*

interface UserApi {

    suspend fun login(username: String, password: String): Boolean
    fun logout()

    suspend fun getUserTypes():List<UserTypesDTO>
    suspend fun findById(id: UUID): UserRegisterRequest?
    suspend fun findUserResponseById(id: UUID): UserResponse?
    suspend fun getUsers(page: Int =DEFAULT_PAGE,
                         pageSize: Int= DEFAULT_PAGE_SIZE) :PaginatedResponse<UserResponse>?
    suspend fun createUser(user: UserRegisterRequest) : ApiResponse
    suspend fun  getUsersUnPaged() : List<UserRegisterRequest>
    suspend fun  getServerHealth(): SimpleResponse
    suspend fun  updateUser(user: UserRegisterRequest): ApiResponse
    suspend fun   deleteUserId(userId: UUID): ApiResponse
    suspend fun  getCasualUsers(page: Int,pageSize: Int, queryString : String? = null) :PaginatedResponse<UserResponse>?
    suspend fun  getFilteredUsers (
        page: Int,
        pageSize: Int,filters: WorkDataFiltersDTO
    ) : PaginatedResponse<UserResponse>?


}
