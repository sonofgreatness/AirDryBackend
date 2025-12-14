package org.montigny.client.dto.Work

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class CreateStackCodeDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = UUID.randomUUID(),
    val displayCode: String? = null,
    val displayName: String? = null,
)
