package org.montigny.client.persistence.repository

import org.montigny.client.domain.api.dto.User.CurrentUserDTO
import org.montigny.client.domain.api.dto.User.UserRegisterRequest
import org.montigny.client.domain.api.dto.User.UserResponse
import java.util.*

interface UserRepository {
    fun createUser(req: UserRegisterRequest): UUID
    fun createAdminUser(req: UserRegisterRequest): UUID
    fun findById(id: UUID): UserRegisterRequest?
    fun findByPhone(phone: String): UserRegisterRequest?
    fun getUsers(currentUser: CurrentUserDTO) :List<UserResponse>
    fun approveUser(id: UUID): Int
    fun updateUser(id: UUID, req: UserRegisterRequest): Int
    fun authenticateByPhoneAndPassword(email: String, password: String): UserRegisterRequest?
    fun getUserTypeDisplayName(userTypeId: UUID): String?









}
