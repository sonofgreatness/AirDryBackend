package org.montigny.client.domain.api.dto.User

import kotlinx.serialization.Serializable
import org.montigny.client.domain.api.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class UserTypesDTO(
    @Serializable(with = UUIDSerializer::class)
    val id : UUID,
    val displayName: String
) {
}