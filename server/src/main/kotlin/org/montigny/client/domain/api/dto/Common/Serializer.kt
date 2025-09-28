package org.montigny.client.domain.api.dto.Common

import io.bkbn.kompendium.oas.security.BasicAuth
import io.bkbn.kompendium.oas.security.BearerAuth
import io.bkbn.kompendium.oas.security.SecuritySchema
import io.bkbn.kompendium.oas.serialization.SecuritySchemaSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.serializer
import java.util.UUID
import kotlin.reflect.KClass
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import java.util.Base64

object UUIDSerializer : KSerializer<UUID?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: UUID?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): UUID? {
        val str = decoder.decodeString()
        return if (str.isBlank()) null else UUID.fromString(str)
    }
}



object AnySerializer : KSerializer<Any> {
    private val jsonElementSerializer = JsonElement.serializer()

    override val descriptor: SerialDescriptor = jsonElementSerializer.descriptor

    @OptIn(InternalSerializationApi::class,
        ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Any) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw IllegalStateException("This serializer can only be used with JSON.")
        val contextualSerializer = ContextualSerializer(Any::class, null, emptyArray())

        when (value) {
            // New case to handle the outer MediaType.Example object
            is io.bkbn.kompendium.oas.payload.MediaType.Example -> {
                // We serialize the inner `value` property using the contextual serializer
                contextualSerializer.serialize(encoder, value.value)
            }
            is BearerAuth -> {
                SecuritySchemaSerializer.serialize(encoder, value)
            }
            is Number -> Json.encodeToJsonElement(value)
            is String -> Json.encodeToJsonElement(value)
            is Boolean -> Json.encodeToJsonElement(value)
            is JsonElement -> jsonEncoder.encodeJsonElement(value)
            else -> {
                // If it's a custom serializable class, get its serializer and use it
                val kClass = value::class as KClass<Any>
                val serializer = jsonEncoder.json.serializersModule.getContextual(kClass)
                    ?: kClass.serializer()
                serializer.serialize(encoder, value)
            }
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer can only be used with JSON.")
        return jsonDecoder.decodeJsonElement()
    }
}

object ByteArrayBase64Serializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ByteArrayBase64__", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ByteArray) {
        val base64 = Base64.getEncoder().encodeToString(value)
        encoder.encodeString(base64)
    }
    override fun deserialize(decoder: Decoder): ByteArray {
        val base64 = decoder.decodeString()
        return Base64.getDecoder().decode(base64)
    }
}