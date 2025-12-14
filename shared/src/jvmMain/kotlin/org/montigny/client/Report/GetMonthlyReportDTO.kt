package org.montigny.client.Report

import kotlinx.serialization.Serializable
import org.montigny.client.dto.Common.UUIDNonNullableSerializer
import java.util.UUID


@Serializable
data class GetMonthlyReportDTO (
    @Serializable(with = UUIDNonNullableSerializer::class)
    val id : UUID,
    val fileName : String,
    val uploadTime : String,
    val fileData : ByteArray,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GetMonthlyReportDTO

        if (id != other.id) return false
        if (fileName != other.fileName) return false
        if (uploadTime != other.uploadTime) return false
        if (!fileData.contentEquals(other.fileData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + uploadTime.hashCode()
        result = 31 * result + fileData.contentHashCode()
        return result
    }
}