package com.kova700.bookchat.core.fcm.service.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = FcmMessageSerializer::class)
data class FcmMessage(
	@SerialName("pushType") val pushType: PushType,
	@SerialName("body") val body: FcmBody,
)

@Serializable
sealed interface FcmBody

@Serializable
data object ForcedLogoutFcmBody : FcmBody

@Serializable
data class ChatFcmBody(
	@SerialName("chatId")
	val chatId: Long,
	@SerialName("chatRoomId")
	val channelId: Long,
	@SerialName("senderId")
	val senderId: Long,
) : FcmBody


object FcmMessageSerializer : KSerializer<FcmMessage> {
	override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FcmMessage") {
		element<String>("pushType")
		element<JsonElement>("body")
	}

	override fun serialize(encoder: Encoder, value: FcmMessage) {
		val jsonEncoder = encoder as? JsonEncoder ?: error("Can be serialized only by JSON")
		val jsonObject = JsonObject(
			mapOf(
				"pushType" to JsonPrimitive(value.pushType.name),
				"body" to jsonEncoder.json.encodeToJsonElement(value.body)
			)
		)
		jsonEncoder.encodeJsonElement(jsonObject)
	}

	override fun deserialize(decoder: Decoder): FcmMessage {
		val jsonDecoder = decoder as? JsonDecoder ?: error("Can be deserialized only by JSON")
		val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
		val pushType =
			PushType.valueOf(jsonObject["pushType"]?.jsonPrimitive?.content ?: error("Missing pushType"))
		val body = when (pushType) {
			PushType.LOGIN -> ForcedLogoutFcmBody
			PushType.CHAT -> jsonDecoder.json.decodeFromJsonElement<ChatFcmBody>(
				jsonObject["body"] ?: error("Missing body")
			)
		}
		return FcmMessage(pushType, body)
	}
}