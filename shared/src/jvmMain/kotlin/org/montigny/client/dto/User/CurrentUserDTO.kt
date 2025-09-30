package org.montigny.client.dto.User

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class CurrentUserDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID?,
    val name: String,
    val surname: String,
    val email: String?,
    val phoneNumber: String,

    // UserType
    @Serializable(with = UUIDSerializer::class)
    val userTypeId: UUID?,
    val userTypeName: String?,   // 👈 new

    // Organization
    @Serializable(with = UUIDSerializer::class)
    val organizationId: UUID?,
    val organizationName: String?,  // 👈 new



    // Department
    @Serializable(with = UUIDSerializer::class)
    val departmentId: UUID?,
    val departmentName: String?,    // 👈 new

    // Section
    @Serializable(with = UUIDSerializer::class)
    val sectionId: UUID?,
    val sectionName: String?,       // 👈 new
    )
