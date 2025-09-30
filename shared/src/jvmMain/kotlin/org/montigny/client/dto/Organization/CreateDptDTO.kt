package org.montigny.client.dto.Organization

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class CreateDptDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID?,
    val displayName: String,
    @Serializable(with = UUIDSerializer::class)
    val organizationId: UUID?)