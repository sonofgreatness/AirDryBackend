package org.montigny.client.dto.Common

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Long,
    val totalPages: Int
)
