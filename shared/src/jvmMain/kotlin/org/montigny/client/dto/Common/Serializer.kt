package org.montigny.client.dto.Common

import io.bkbn.kompendium.oas.security.BearerAuth
import io.bkbn.kompendium.oas.serialization.SecuritySchemaSerializer
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass




// ---------------------------
// Non-nullable UUID Serializer
// ---------------------------
object UUIDNonNullableSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUIDNonNullable", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        val str = decoder.decodeString()
        return UUID.fromString(str)
    }
}




// ---------------------------
// Non-nullable List<UUID> Serializer
// ---------------------------
object UUIDNonNullableListSerializer : KSerializer<List<UUID>> {
    private val uuidSerializer = UUIDNonNullableSerializer

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("List<UUID>", StructureKind.LIST)

    override fun serialize(encoder: Encoder, value: List<UUID>) {
        val composite = encoder.beginCollection(descriptor, value.size)
        value.forEachIndexed { index, uuid ->
            composite.encodeSerializableElement(descriptor, index, uuidSerializer, uuid)
        }
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): List<UUID> {
        val dec = decoder.beginStructure(descriptor)
        val list = mutableListOf<UUID>()
        while (true) {
            val index = dec.decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break
            list.add(dec.decodeSerializableElement(descriptor, index, uuidSerializer))
        }
        dec.endStructure(descriptor)
        return list
    }
}
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


// ---------------------------
// Date Serializer
/*// When saving to DB
val instant = workDoneDTO.workDate.toInstant()

// When reading from DB
val date = Date.from(row[WorkDone.workDate])*/
// ---------------------------
// Converts Date <-> ISO 8601 string (e.g., "2025-10-13T08:45:00Z")
object DateSerializer : KSerializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(dateFormat.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        val str = decoder.decodeString()
        return dateFormat.parse(str)
            ?: throw SerializationException("Invalid date format: $str")
    }
}