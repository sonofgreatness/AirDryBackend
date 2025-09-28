package org.montigny.client.domain.api.dto.Organization

import kotlinx.serialization.Serializable
import org.montigny.client.domain.api.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class CreateOrgDTO(
    @Serializable(with = UUIDSerializer::class)
    val id : UUID? = UUID.randomUUID(),
    val displayName : String = ""
)