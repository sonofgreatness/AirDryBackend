package org.montigny.client.service

import org.montigny.client.config.JwtProvider
import org.montigny.client.domain.api.dto.User.CurrentUserDTO
import org.montigny.client.domain.api.dto.User.UserRegisterRequest
import org.montigny.client.domain.api.dto.User.UserResponse
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.persistence.repository.UserRepository
import java.util.UUID

class UserService(private val userRepo: UserRepository, private val jwtProvider: JwtProvider) {
    fun registerUser(req: UserRegisterRequest): UUID {
        return userRepo.createUser(req)
    }

    fun registerAdmin(req: UserRegisterRequest): UUID {
        return userRepo.createAdminUser(req)
    }


    fun getUsers(currentUser: CurrentUserDTO) :List<UserResponse>{
        return userRepo.getUsers(currentUser)
    }

    fun approveUser(id: UUID) {
        // maybe check admin privileges before calling
        userRepo.approveUser(id)
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


}
