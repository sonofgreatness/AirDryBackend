package org.montigny.client.dto.Organization

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.ByteArrayBase64Serializer
import org.montigny.client.dto.Common.UUIDSerializer
import java.util.UUID
@Serializable
data class OrganizationResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID?,
    val name: String,
    @Serializable(with = ByteArrayBase64Serializer::class)
    val logo: ByteArray? = null,
    val departments: List<DepartmentResponse> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrganizationResponse

        if (id != other.id) return false
        if (name != other.name) return false
        if (!logo.contentEquals(other.logo)) return false
        if (departments != other.departments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + (logo?.contentHashCode() ?: 0)
        result = 31 * result + departments.hashCode()
        return result
    }
}

@Serializable
data class DepartmentResponse(
    @Serializable(with = UUIDSerializer::class)
    val id : UUID? = UUID.randomUUID(),
    val displayName : String = "",
    val sections: List<CreateSectDTO> = emptyList()
)
