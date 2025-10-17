package org.montigny.client.dto.Work

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.DateSerializer
import org.montigny.client.dto.Common.UUIDNonNullableListSerializer
import org.montigny.client.dto.Common.UUIDNonNullableSerializer
import java.util.*

@Serializable
data class CreateWorkDoneDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id : UUID = UUID.randomUUID(),
    @Serializable(with = DateSerializer::class)
    val workDate: Date,
    val  workersList  : List<WorkDoneUsersDTO>,
    val  stacksList : List<WorkDoneStacksDTO>
    ) {
    fun generateCreateWorkDTOStringAsDate(): CreateWorkDTOStringAsDate {
        return CreateWorkDTOStringAsDate(
            id=id,
            workDate = getFormattedDate(workDate),
            workersList = workersList,
            stacksList = stacksList
        )
}

@Serializable
data class WorkDoneUsersDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id : UUID,
    @Serializable(with = UUIDNonNullableListSerializer::class)
     val userIds : List<UUID> = emptyList(),
)
@Serializable
data class WorkDoneStacksDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id : UUID,
    @Serializable(UUIDNonNullableListSerializer::class)
    val  stackId : List<UUID> =emptyList())


private fun getFormattedDate(date: Date): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }
}