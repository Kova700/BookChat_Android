package com.example.bookchat.data.database.typeconverter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

@ProvidedTypeConverter
class ParticipantAuthoritiesTypeConverter @Inject constructor(
	private val gson: Gson
) {
	@TypeConverter
	fun mapToJson(value: Map<Long, ChannelMemberAuthority>?): String? {
		if (value == null) return null
		return gson.toJson(value)
	}

	@TypeConverter
	fun jsonToMap(value: String?): Map<Long, ChannelMemberAuthority>? {
		if (value == null) return null
		return gson.fromJson(value, object : TypeToken<Map<Long, ChannelMemberAuthority>>() {}.type)
	}

	@TypeConverter
	fun authorityToString(value: ChannelMemberAuthority) = value.name

	@TypeConverter
	fun stringToAuthority(value: String) = ChannelMemberAuthority.valueOf(value)
}