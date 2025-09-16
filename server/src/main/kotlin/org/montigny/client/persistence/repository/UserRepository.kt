package org.montigny.client.persistence.repository

import org.montigny.client.domain.api.dto.Common.PaginatedResponse
import org.montigny.client.domain.api.dto.User.CurrentUserDTO
import org.montigny.client.domain.api.dto.User.UpdateUserDTO
import org.montigny.client.domain.api.dto.User.UserRegisterRequest
import org.montigny.client.domain.api.dto.User.UserResponse
import org.montigny.client.domain.api.dto.User.UserTypesDTO
import java.util.*

interface UserRepository {
    fun createUser(req: UserRegisterRequest): UUID
    fun createAdminUser(req: UserRegisterRequest): UUID
    fun findById(id: UUID): UserRegisterRequest?
    fun findByPhone(phone: String): UserRegisterRequest?
    fun getUsers(currentUser: CurrentUserDTO,page: Int,pageSize: Int) :PaginatedResponse<UserResponse>
    fun getUserTypes():List<UserTypesDTO>
    fun updateUser(id: UUID, req: UpdateUserDTO): Int
    fun authenticateByPhoneAndPassword(email: String, password: String): UserRegisterRequest?
    fun getUserTypeDisplayName(userTypeId: UUID): String?








}
