package org.montigny.client.domain.api.dto.User

import kotlinx.serialization.Serializable
import org.montigny.client.domain.api.dto.Common.UUIDSerializer
import java.util.UUID

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
)

