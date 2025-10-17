package org.montigny.client.dto.Work

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDNonNullableSerializer
import java.util.*

@Serializable
data class CreateWorkDTOStringAsDate(
    @Serializable(with = UUIDNonNullableSerializer::class)
val id : UUID = UUID.randomUUID(),
val workDate: String,
    val  workersList  : List<CreateWorkDoneDTO.WorkDoneUsersDTO>,
    val  stacksList : List<CreateWorkDoneDTO.WorkDoneStacksDTO>
)