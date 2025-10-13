package org.montigny.client.dto.Work

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class GetStackDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID?,
    val length: Float,
    val height: Float,
    val thickness: Float,
    val displayName: String,
    val stackCode: StackEntityResponse
)
@Serializable
data class StackEntityResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = UUID.randomUUID(),
    val displayCode: String,
    val displayName: String
)