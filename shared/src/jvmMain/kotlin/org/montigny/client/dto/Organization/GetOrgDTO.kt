package org.montigny.client.dto.Organization

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.ByteArrayBase64Serializer
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID
@Serializable
data class GetOrgDTO(
    @Serializable(with = UUIDSerializer::class)
    val id : UUID? = UUID.randomUUID(),
    val displayName : String? = "",
    @Serializable(with = ByteArrayBase64Serializer::class)
    val logo: ByteArray? = null
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GetOrgDTO) return false

        if (id != other.id) return false
        if (displayName != other.displayName) return false
        if (logo != null) {
            if (other.logo == null) return false
            if (!logo.contentEquals(other.logo)) return false
        } else if (other.logo != null) return false
        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + (logo?.contentHashCode() ?: 0)
        return result
    }
    fun genererateCreateOrgDto() : CreateOrgDTO {
        return CreateOrgDTO(
            id = this.id,
            displayName = this.displayName!!,
        )
    }
}
