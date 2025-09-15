package org.montigny.client.domain.api.dto.User


import kotlinx.serialization.Serializable
import org.montigny.client.domain.api.dto.Common.UUIDSerializer
import java.util.UUID


@Serializable
data class UserResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val secondaryPhoneNumber: String?,
    val email: String?,
    val isActive: Boolean,
    val isApproved: Boolean,

    // Resolved references
    val userType: NamedEntityResponse?,
    val organization: NamedEntityResponse?,
    val department: NamedEntityResponse?,
    val section: NamedEntityResponse?
)

@Serializable
data class NamedEntityResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val displayName: String
)
