package org.montigny.client.dto.Common

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ReleaseDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id : UUID,
    val dateOfRelease : String,
    val link : String,
    val platform : String)
