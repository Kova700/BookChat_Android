package com.example.bookchat.data.local.typeconverter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson

@ProvidedTypeConverter
class StringListTypeConverter(
    private val gson: Gson
) {
    @TypeConverter
    fun listToJson(value: List<String>?): String? {
        if (value == null) return null
        return gson.toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String?): List<String>? {
        if (value == null) return null
        return gson.fromJson(value, Array<String>::class.java).toList()
    }
}