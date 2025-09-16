package org.montigny.client.domain.api.dto.Common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class IdResponse(val id: String)

@Serializable
data class SimpleResponse(val message: String)

@Serializable
data class ErrorResponse(
    val error: String,
    val code: Int
)



//KEEP FOR POSTERITY used to patch kompedium
@Serializable
data class CustomBearerSecurityRequirement(
    val name: String = "BearerAuth",
    val type: String = "http",
    val scheme: String = "bearer",
    val bearerFormat: String = "JWT"
){
    fun toJson(): JsonElement {
        return buildJsonObject {
            put("type", type)
            put("scheme", scheme)
            put("bearerFormat", bearerFormat)
        }
    }
}