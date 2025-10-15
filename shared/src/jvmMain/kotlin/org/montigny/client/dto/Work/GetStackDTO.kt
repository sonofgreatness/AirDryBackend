package org.montigny.client.dto.Work

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID

@Serializable
data class GetStackDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID?,
    val length: Float,
    val height: Float,
    val thickness: Float,
    val displayName: String,
    val numberOfPieces : Int,
    val volume : Double,
    val stackCode: StackEntityResponse
)
{
    fun  generateCreateStackDTO() : CreateStackDTO
    {
        return CreateStackDTO(
            id =this.id,
            length = this.length,
            height = this.height,
            thickness = this.thickness,
            displayName = this.displayName,
            numberOfPieces = this.numberOfPieces,
            stackCodeId = this.stackCode.id
        )
    }
}
@Serializable
data class StackEntityResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = UUID.randomUUID(),
    val displayCode: String,
    val displayName: String
)
