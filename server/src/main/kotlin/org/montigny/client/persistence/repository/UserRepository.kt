package org.montigny.client.persistence.repository

import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.User.UpdateUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.User.UserTypesDTO
import java.util.*

interface UserRepository {
    fun createUser(currentUserDTO: CurrentUserDTO, req: UserRegisterRequest): UUID
    fun createAdminUser(req: UserRegisterRequest): UUID
    fun findById(id: UUID): UserRegisterRequest?
    fun findByPhone(phone: String): UserRegisterRequest?
    fun getUsers(currentUser: CurrentUserDTO,page: Int,pageSize: Int) :PaginatedResponse<UserResponse>
    fun getCasualUsers(currentUser: CurrentUserDTO,page: Int,pageSize: Int, queryString : String? = null) :PaginatedResponse<UserResponse>
    fun getUserTypes():List<UserTypesDTO>
    fun updateUser(id: UUID, req: UpdateUserDTO): Int
    fun authenticateByPhoneAndPassword(email: String, password: String): UserRegisterRequest?
    fun getUserTypeDisplayName(userTypeId: UUID): String?
    fun deleteUserId(userId: UUID): Int
    fun getUsersFiltered(
        currentUser: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        filters: MutableMap<String, String>
    ):PaginatedResponse<UserResponse>
    fun getUserUnPaginated(currentUserDTO: CurrentUserDTO):List<UserRegisterRequest>








}
