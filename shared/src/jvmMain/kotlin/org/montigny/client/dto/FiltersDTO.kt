package org.montigny.client.dto

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class  WorkDataFiltersDTO (
    val includeInactiveWorkers: Boolean = false,
    val  filterDate : String? = null,
    @Serializable(with = UUIDSerializer::class)
    val  filterDepartmentId : UUID? = null,
    @Serializable(with = UUIDSerializer::class)
    val  filterSectionId :UUID? = null,
    val  filterName :String? = null,
    val  filterSurname : String? = null,
    val  filterPhoneNumber : String? = null,
    val  filterSecondaryPhoneNumber : String? = null,
    val filterDateEndDate : String? = null,
    @Serializable(with = UUIDSerializer::class)
    val  filterUserId : UUID? = null,
    val includeOtherStaffRecords : Boolean = true)