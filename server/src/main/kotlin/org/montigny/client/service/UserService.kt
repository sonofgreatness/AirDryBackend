package org.montigny.client.service

import org.montigny.client.config.JwtProvider
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.User.UpdateUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.persistence.repository.UserRepository
import java.util.UUID

class UserService(private val userRepo: UserRepository, private val jwtProvider: JwtProvider) {
    fun registerUser(currentUserDTO: CurrentUserDTO,req: UserRegisterRequest): UUID {
        return userRepo.createUser(currentUserDTO,req)
    }
    fun registerAdmin(req: UserRegisterRequest): UUID {
        return userRepo.createAdminUser(req)
    }
    fun getUsers(currentUser: CurrentUserDTO,page: Int,pageSize: Int) :PaginatedResponse<UserResponse>{
        return userRepo.getUsers(currentUser,page,pageSize)
    }
    fun getCasualUsers(currentUser: CurrentUserDTO,page: Int,pageSize: Int, queryString : String? = null):PaginatedResponse<UserResponse>{
        return  userRepo.getCasualUsers(currentUser,page,pageSize, queryString)
    }
    fun getUserTypes():List<UserTypesDTO>{
        return userRepo.getUserTypes()
    }
    fun updateUser(id: UUID,req: UpdateUserDTO){
        userRepo.updateUser(id,req)
    }
    fun login(phone: String, password: String): String {
        val user = userRepo.authenticateByPhoneAndPassword(phone, password)
            ?: throw IllegalArgumentException("Invalid credentials")
        if (!user.isActive || !user.isApproved) throw IllegalStateException("User not active or approved")
        return jwtProvider.generateToken(user)
    }

    fun resolveUserRole(userTypeId: String): UserRole? {
        return try {
            val uuid = UUID.fromString(userTypeId)
            val displayName = userRepo.getUserTypeDisplayName(uuid)
            displayName?.let { UserRole.fromString(it) }
        } catch (e: IllegalArgumentException) {
            null // invalid UUID
        }
    }
    fun deleteUserId(userId: UUID): Int = userRepo.deleteUserId(userId)

   fun getUsersFiltered(
    currentUser: CurrentUserDTO,
    page: Int,
    pageSize: Int,
    filters: MutableMap<String, String>
    ):PaginatedResponse<UserResponse> =userRepo.getUsersFiltered(currentUser,page,pageSize,filters);

    fun findById(id: UUID): UserRegisterRequest? = userRepo.findById(id)
    fun findUserResponseById(id: UUID): UserResponse? = userRepo.findUserResponseById(id)
    fun getUserUnPaginated(currentUserDTO: CurrentUserDTO):List<UserRegisterRequest> = userRepo.getUserUnPaginated(currentUserDTO)

    fun getUsersFilteredWithDTO(
        currentUser: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        filters: WorkDataFiltersDTO
    ): PaginatedResponse<UserResponse>
    = userRepo.getUsersFilteredWithDTO(currentUser,page,pageSize,filters)

}

