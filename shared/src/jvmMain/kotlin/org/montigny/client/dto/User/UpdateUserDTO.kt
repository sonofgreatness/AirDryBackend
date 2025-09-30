package org.montigny.client.dto.User

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID
@Serializable
data class UpdateUserDTO(
    val updated_name: String? = null,
    val updated_surname: String? = null,
    val updated_phoneNumber: String? = null,
    val updated_secondaryPhoneNumber: String? = null,
    @Serializable(with = UUIDSerializer::class)
    val updated_userTypeId: UUID? = null,

    @Serializable(with = UUIDSerializer::class)
    val updated_departmentId: UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val updated_sectionId: UUID? = null,
    val updated_email: String? = null,
    val updated_password: String? = null,
    val isActive: Boolean? = null,
    val isApproved: Boolean? = null,
)
