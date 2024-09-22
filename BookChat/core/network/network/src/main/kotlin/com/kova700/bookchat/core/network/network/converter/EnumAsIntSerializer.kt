package com.kova700.bookchat.core.network.network.converter

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

open class EnumAsIntSerializer<T : Enum<*>>(
	val codeGetter: (enum: T) -> Int,
	val enumGetter: (code: Int) -> T,
) : KSerializer<T> {
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("EnumAsIntSerializer", PrimitiveKind.INT)

	override fun serialize(encoder: Encoder, value: T) {
		encoder.encodeInt(codeGetter(value))
	}

	override fun deserialize(decoder: Decoder): T {
		return enumGetter(decoder.decodeInt())
	}
}