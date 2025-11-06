package org.montigny.client.dto.User


import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID


@Serializable
data class UserResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val nationalId: String?,
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
    val id: UUID?,
    val displayName: String
)
