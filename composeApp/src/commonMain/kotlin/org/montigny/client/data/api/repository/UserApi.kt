package org.montigny.client.data.api.repository

import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.User.UserTypesDTO
import java.util.*

interface UserApi {

    suspend fun login(username: String, password: String): Boolean
    fun logout()

    suspend fun getUserTypes():List<UserTypesDTO>
    suspend fun findById(id: UUID): UserRegisterRequest?
    suspend fun getUsers(page: Int=1,pageSize: Int=10) :PaginatedResponse<UserResponse>
}