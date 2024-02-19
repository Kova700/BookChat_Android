package com.example.bookchat.data.local.typeconverter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson

@ProvidedTypeConverter
class LongListTypeConverter(
    private val gson: Gson
) {
    @TypeConverter
    fun listToJson(value: List<Long>?): String? {
        if (value == null) return null
        return gson.toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String?): List<Long>? {
        if (value == null) return null
        return gson.fromJson(value, Array<Long>::class.java).toList()
    }
}