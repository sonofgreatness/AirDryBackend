package org.montigny.client.dto.User

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID
import javax.naming.Name

@Serializable
data class UserRegisterRequest(
    val name: String = "John",
    val surname: String = "Doe",
    val phoneNumber: String = "76000000",
    val secondaryPhoneNumber: String? = "79000000",
    @Serializable(with = UUIDSerializer::class)
    val userTypeId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val organizationId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val departmentId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val sectionId: UUID? = null,
    val email: String? = "john@email.com",
    val password: String = "password",
    val isActive: Boolean = false,
    val isApproved: Boolean = false,
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
){
    fun toUpdateUserDTO(): UpdateUserDTO {
        return UpdateUserDTO(
            updated_name = name,updated_surname = surname,updated_phoneNumber = phoneNumber,
            updated_secondaryPhoneNumber = secondaryPhoneNumber, updated_password = password,
            updated_sectionId = sectionId, updated_userTypeId = userTypeId, updated_departmentId = departmentId,
            updated_email = email, isApproved = isApproved, isActive = isActive
        )
    }
    fun toUserResponseDTO(): UserResponse {
        return UserResponse(
            id = id!!,
            name = name,
            surname = surname,
            phoneNumber = phoneNumber,
            secondaryPhoneNumber = secondaryPhoneNumber,
            email = email,
            isActive = isActive,
            isApproved = isApproved,
            userType = NamedEntityResponse(
                id = userTypeId!!,
                displayName = name,
            ),
            organization = NamedEntityResponse(
                id = organizationId!!,
                displayName = name,
            ),
            department = NamedEntityResponse(
                id = departmentId!!,
                displayName = name,
            ),
            section = NamedEntityResponse(
                id = sectionId!!,
                displayName = name),
        )
    }
}

