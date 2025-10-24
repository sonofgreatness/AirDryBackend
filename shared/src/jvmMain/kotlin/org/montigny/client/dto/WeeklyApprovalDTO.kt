package org.montigny.client.dto

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDNonNullableSerializer
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.*


@Serializable
data class WeeklyApprovalDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDNonNullableSerializer::class)
    val organizationId: UUID,
    val weekStartDate: String,
    val weekEndDate: String,
    @Serializable(with = UUIDSerializer::class)
    val approvedById: UUID? = null,
    val approvedAt: String? = null,
    val status: String,
    val notes: String? = null
)
@Serializable
data class CreateWeeklyApprovalDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val organizationId: UUID,
    val weekStartDate: String,
    val weekEndDate: String,
    val notes: String? = null
)
@Serializable
data class UpdateWeeklyApprovalDTO(
 @Serializable(with = UUIDNonNullableSerializer::class)
    val id: UUID,
    val status: String,
    val notes: String? = null
)