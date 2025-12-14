package org.montigny.client.dto.Work

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class CreateStackDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = UUID.randomUUID(),
    val length: Float? = null,
    val height: Float? = null,
    val thickness: Float? = null,
    val numberOfPieces: Int? = null,
    val displayName: String? = null,
    @Serializable(with = UUIDSerializer::class)
    val stackCodeId: UUID?
)