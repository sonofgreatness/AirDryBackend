package org.montigny.client.dto.Work

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDSerializer
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
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
    fun printDimensionsAndVolume(): String {
        val safeDisplayName = displayName.ifBlank { "Unnamed Stack" }
        val safeStackCode = stackCode.displayName.ifBlank { "N/A" }

        try{
            val symbols = DecimalFormatSymbols(Locale.US)
            val dfDim = DecimalFormat("0.000", symbols)
            val dfVol = DecimalFormat("0.00", symbols)
            // --- REPLACEMENT FOR buildString: Triple-quoted string literal ---
            val result = "Stack:$safeDisplayName  Dms:${dfDim.format(length)}×${dfDim.format(height)}×${dfDim.format(thickness)} \n Pieces: $numberOfPieces   Cubes: ${dfVol.format(volume)}m³".trim()
            return result

        }catch (e:Exception){
            println("failed to build string at GetStackDTO.printDimensionsAndVolume: ${e.message}")
            println("${length.toDouble()} ${thickness.toDouble()} ${height.toDouble()}")
        return stackCode.displayName
        }
    }
    }
@Serializable
data class StackEntityResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = UUID.randomUUID(),
    val displayCode: String,
    val displayName: String
)
