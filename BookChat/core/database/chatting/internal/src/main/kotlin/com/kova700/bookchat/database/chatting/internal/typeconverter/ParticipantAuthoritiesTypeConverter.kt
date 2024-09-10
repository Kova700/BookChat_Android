package com.kova700.bookchat.database.chatting.internal.typeconverter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@ProvidedTypeConverter
class ParticipantAuthoritiesTypeConverter @Inject constructor() {
	@TypeConverter
	fun mapToJson(value: Map<Long, ChannelMemberAuthority>?): String? {
		if (value == null) return null
		return Json.encodeToString(value)
	}

	@TypeConverter
	fun jsonToMap(value: String?): Map<Long, ChannelMemberAuthority>? {
		if (value == null) return null
//		return gson.fromJson(value, object : TypeToken<Map<Long, ChannelMemberAuthority>>() {}.type)
		return Json.decodeFromString<Map<Long, ChannelMemberAuthority>>(value)
	}

	@TypeConverter
	fun authorityToString(value: ChannelMemberAuthority) = value.name

	@TypeConverter
	fun stringToAuthority(value: String) = ChannelMemberAuthority.valueOf(value)
}