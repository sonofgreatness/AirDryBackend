package org.montigny.client.dto.User

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class UserTypesDTO(
    @Serializable(with = UUIDSerializer::class)
    val id : UUID? = null,
    val displayName: String
) {
}