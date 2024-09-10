package com.kova700.bookchat.database.chatting.internal.typeconverter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@ProvidedTypeConverter
class StringListTypeConverter @Inject constructor() {
	@TypeConverter
	fun listToJson(value: List<String>?): String? {
		if (value == null) return null
		return Json.encodeToString(value)
	}

	@TypeConverter
	fun jsonToList(value: String?): List<String>? {
		if (value == null) return null
		return Json.decodeFromString<List<String>>(value)
	}
}