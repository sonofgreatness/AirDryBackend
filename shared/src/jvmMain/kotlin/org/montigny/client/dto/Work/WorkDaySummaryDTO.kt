package org.montigny.client.dto.Work
import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDNonNullableSerializer
import java.util.*

@Serializable
data class WorkDaySummaryDTO(
    val dayLabel: String,
    val totalVolume: Double,
    val totalStacks: Int,
    val totalUsers: Int,
    val workList: List<WorkSummaryDTO>
)

@Serializable
data class WorkSummaryDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id: UUID,
    val workDate: String,
    val totalVolume: Double,
    val totalStacks: Int,
    val totalUsers: Int,
    val stacks: List<StackInfoDTO>,
    val users: List<UserInfoDTO>,
    //NEW
    val  capturedAt : String?=null,
    val updatedAt : String?=null,
    val  capturedBy: String?=null,
    val updatedBy: String?=null,
//END  OF NEW
    )

@Serializable
data class StackInfoDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id: UUID,
    val displayName: String,
    val stackCodeDisplayName: String,
    val stackCode:String,
    val volume: Double
)


@Serializable
data class UserInfoDTO(
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id: UUID,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val isActive : Boolean,
    val isApproved: Boolean,
    val personalCubes: Double
)
